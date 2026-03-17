package com.example.devflow.ui.contests

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.devflow.AlarmScheduler
import com.example.devflow.data.model.Contest
import com.example.devflow.ui.components.AppTopBar
import com.example.devflow.ui.components.EaseInOutSine
import com.example.devflow.ui.components.GlassCard
import com.example.devflow.ui.theme.*
import com.example.devflow.worker.ContestReminderWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestsScreen(
    navController: NavController,
    viewModel: ContestViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val remindersSet = remember { mutableStateListOf<Int>() }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = "Codeforces",
                navController = navController,
                extraActions = {
                    IconButton(onClick = { viewModel.fetchContests() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ContestUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = LGPrimary)
                        Text("Fetching contests...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp)
                    }
                }

                is ContestUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⚠️ ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp)
                        Button(
                            onClick = { viewModel.fetchContests() },
                            colors = ButtonDefaults.buttonColors(containerColor = LGPrimary)
                        ) { Text("Retry") }
                    }
                }

                is ContestUiState.Success -> {
                    if (state.contests.isEmpty()) {
                        Box(
                            modifier = Modifier.align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            GlassCard(modifier = Modifier.padding(32.dp)) {
                                Text(
                                    "No upcoming contests found",
                                    modifier = Modifier.padding(24.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(4.dp)) }
                            items(state.contests) { contest ->
                                var showSheet by remember { mutableStateOf(false) }

                                if (showSheet) {
                                    ContestDetailSheet(
                                        contest = contest,
                                        hasReminder = remindersSet.contains(contest.id),
                                        onDismiss = { showSheet = false },
                                        onToggleReminder = { minutesBefore ->
                                            if (remindersSet.contains(contest.id)) {
                                                cancelContestReminder(context, contest.id)
                                                remindersSet.remove(contest.id)
                                            } else {
                                                scheduleContestReminder(context, contest, minutesBefore)
                                                remindersSet.add(contest.id)
                                            }
                                            showSheet = false
                                        },
                                        onAddAsTask = {
                                            showSheet = false
                                            val encodedName = java.net.URLEncoder.encode(
                                                contest.name, "UTF-8"
                                            )
                                            navController.navigate(
                                                "add_task_cf/$encodedName/${contest.startTimeMillis}"
                                            )
                                        }
                                    )
                                }

                                LiquidContestCard(
                                    contest = contest,
                                    hasReminder = remindersSet.contains(contest.id),
                                    onToggleReminder = { minutesBefore ->
                                        if (remindersSet.contains(contest.id)) {
                                            cancelContestReminder(context, contest.id)
                                            remindersSet.remove(contest.id)
                                        } else {
                                            scheduleContestReminder(context, contest, minutesBefore)
                                            remindersSet.add(contest.id)
                                        }
                                    },
                                    onClick = { showSheet = true }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(120.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidContestCard(
    contest: Contest,
    hasReminder: Boolean,
    onToggleReminder: (Int) -> Unit,
    onClick: () -> Unit = {}
) {
    val now = System.currentTimeMillis()
    val diffMillis = contest.startTimeMillis - now
    val countdown = formatCountdown(diffMillis)
    val durationHours = contest.durationSeconds / 3600
    val durationMins = (contest.durationSeconds % 3600) / 60
    val startFormatted = SimpleDateFormat("EEE, MMM d  hh:mm a", Locale.getDefault())
        .format(Date(contest.startTimeMillis))
    val isToday = diffMillis in 0..(24 * 60 * 60 * 1000L)
    val isVeryClose = diffMillis in 0..(3 * 60 * 60 * 1000L)

    val infiniteTransition = rememberInfiniteTransition(label = "contest_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val accentColor = when {
        isVeryClose -> LGError
        isToday -> LGPrimary
        else -> LGTeal
    }

    val gradientColors = when {
        isVeryClose -> listOf(Color(0xFFFF3B30), Color(0xFFFF6B35))
        isToday -> listOf(LGPrimary, LGPurple)
        else -> listOf(LGTeal, LGPrimary)
    }

    var showReminderDialog by remember { mutableStateOf(false) }

    if (showReminderDialog) {
        ReminderOptionsDialog(
            contestName = contest.name,
            onDismiss = { showReminderDialog = false },
            onSelect = { minutes ->
                onToggleReminder(minutes)
                showReminderDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isToday) 16.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = accentColor.copy(alpha = if (isToday) glowAlpha * 0.3f else 0.1f),
                spotColor = accentColor.copy(alpha = if (isToday) glowAlpha * 0.4f else 0.15f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.90f)
                    )
                )
            )
            .border(
                width = if (isToday) 1.5.dp else 0.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = if (isToday) glowAlpha else 0.3f),
                        accentColor.copy(alpha = if (isToday) glowAlpha * 0.3f else 0.1f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = contest.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isToday || isVeryClose) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.linearGradient(colors = gradientColors)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isVeryClose) "SOON" else "TODAY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Gradient divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.5f),
                                accentColor.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("📅  $startFormatted", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "⏱  ${durationHours}h ${if (durationMins > 0) "${durationMins}m" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏳  $countdown",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (hasReminder)
                                accentColor.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (hasReminder) accentColor.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            if (hasReminder) onToggleReminder(30)
                            else showReminderDialog = true
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (hasReminder)
                                Icons.Filled.Notifications
                            else Icons.Filled.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (hasReminder) accentColor
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (hasReminder) "Set" else "Remind",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (hasReminder) accentColor
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

fun scheduleContestReminder(context: Context, contest: Contest, minutesBefore: Int) {
    AlarmScheduler.scheduleContestReminder(
        context, contest.id, contest.name,
        contest.startTimeMillis, minutesBefore
    )
}

fun cancelContestReminder(context: Context, contestId: Int) {
    AlarmScheduler.cancelContestReminder(context, contestId)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestDetailSheet(
    contest: Contest,
    hasReminder: Boolean,
    onDismiss: () -> Unit,
    onToggleReminder: (Int) -> Unit,
    onAddAsTask: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val now = System.currentTimeMillis()
    val diffMillis = contest.startTimeMillis - now
    val countdown = formatCountdown(diffMillis)
    val durationHours = contest.durationSeconds / 3600
    val durationMins = (contest.durationSeconds % 3600) / 60
    val startFormatted = SimpleDateFormat(
        "EEEE, MMM d yyyy  hh:mm a", Locale.getDefault()
    ).format(Date(contest.startTimeMillis))
    val endFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault())
        .format(Date(contest.endTimeMillis))
    val isToday = diffMillis in 0..(24 * 60 * 60 * 1000L)
    var showReminderDialog by remember { mutableStateOf(false) }

    if (showReminderDialog) {
        ReminderOptionsDialog(
            contestName = contest.name,
            onDismiss = { showReminderDialog = false },
            onSelect = { minutes ->
                onToggleReminder(minutes)
                showReminderDialog = false
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = contest.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (isToday) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(LGPrimary, LGPurple)
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("TODAY", fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            ContestDetailRow("📅 Start", startFormatted)
            ContestDetailRow("🏁 End", "~$endFormatted")
            ContestDetailRow(
                "⏱ Duration",
                "${durationHours}h ${if (durationMins > 0) "${durationMins}m" else ""}"
            )
            ContestDetailRow("⏳ Starts in", countdown)
            ContestDetailRow("🏷 Type", contest.type)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // Add as Task button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(LGPrimary, LGPurple)
                        )
                    )
                    .clickable { onAddAsTask() }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(18.dp))
                    Text("Add to My Tasks", fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            // Reminder button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = if (hasReminder) LGPrimary.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable {
                        if (hasReminder) onToggleReminder(30)
                        else showReminderDialog = true
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasReminder) Icons.Filled.Notifications
                        else Icons.Filled.NotificationsOff,
                        contentDescription = null,
                        tint = if (hasReminder) LGPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (hasReminder) "Remove Reminder" else "Set Reminder",
                        fontSize = 15.sp,
                        color = if (hasReminder) LGPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ContestDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp))
        Text(text = value, fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f))
    }
}

@Composable
fun ReminderOptionsDialog(
    contestName: String,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val options = listOf(
        5 to "5 minutes before",
        10 to "10 minutes before",
        30 to "30 minutes before",
        60 to "1 hour before",
        180 to "3 hours before",
        1440 to "1 day before"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Reminder", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Remind me before:", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                options.forEach { (minutes, label) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelect(minutes) }
                            .padding(12.dp)
                    ) {
                        Text(label, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun formatCountdown(millis: Long): String {
    if (millis <= 0) return "Started"
    val days = millis / (1000 * 60 * 60 * 24)
    val hours = (millis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
    val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
    return when {
        days > 0  -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else      -> "${minutes}m"
    }
}