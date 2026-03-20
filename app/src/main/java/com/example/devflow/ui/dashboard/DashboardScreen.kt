package com.example.devflow.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.devflow.data.model.Attendance
import com.example.devflow.data.model.Student
import com.example.devflow.data.model.Task
import com.example.devflow.navigation.Screen
import com.example.devflow.ui.components.GlassCard
import com.example.devflow.ui.components.GradientStatCard
import com.example.devflow.ui.components.AppTopBar
import com.example.devflow.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val todayTasks by viewModel.todayTasks.collectAsState()
    val tomorrowTasks by viewModel.tomorrowTasks.collectAsState()
    val missedTasks by viewModel.missedTasks.collectAsState()
    val todayStudents by viewModel.todayStudents.collectAsState()
    val todayAttendance by viewModel.todayAttendance.collectAsState()
    val todayContests by viewModel.todayContests.collectAsState()

    val today = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Column {
                        Text(
                            "$greeting 👋",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            today,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Focus.route)
                    }) {
                        Icon(Icons.Filled.Timer, contentDescription = "Focus Timer")
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.Settings.route)
                    }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gradient stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GradientStatCard(
                        label = "Today",
                        value = todayTasks.size.toString(),
                        gradientColors = listOf(Color(0xFF6650A4), Color(0xFF4FC3F7)),
                        modifier = Modifier.weight(1f)
                    )
                    GradientStatCard(
                        label = "Missed",
                        value = missedTasks.size.toString(),
                        gradientColors = listOf(Color(0xFFE53935), Color(0xFFFF6B35)),
                        modifier = Modifier.weight(1f)
                    )
                    GradientStatCard(
                        label = "Done",
                        value = todayTasks.count { it.isCompleted }.toString(),
                        gradientColors = listOf(Color(0xFF43A047), Color(0xFF00ACC1)),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Today's CF Contests ──────────────────────
            if (todayContests.isNotEmpty()) {
                item {
                    Text("🏆 Today's Contests",
                        fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                        color = LGPrimary)
                }
                items(todayContests) { contest ->
                    val now = System.currentTimeMillis()
                    val diffMillis = contest.startTimeMillis - now
                    val countdown = when {
                        diffMillis <= 0 -> "Started!"
                        diffMillis < 3600000 -> "${diffMillis / 60000}m left"
                        else -> "${diffMillis / 3600000}h ${(diffMillis % 3600000) / 60000}m left"
                    }
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = if (diffMillis < 3600000) LGError else LGPrimary
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(LGPrimary, LGPurple)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("CF", color = Color.White,
                                    fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contest.name, fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1)
                                Text(
                                    SimpleDateFormat("hh:mm a", Locale.getDefault())
                                        .format(Date(contest.startTimeMillis)),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (diffMillis < 3600000)
                                            LGError.copy(alpha = 0.15f)
                                        else LGPrimary.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(countdown, fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (diffMillis < 3600000) LGError
                                    else LGPrimary)
                            }
                        }
                    }
                }
            }

            // ── Today's Attendance ───────────────────────
            if (todayStudents.isNotEmpty()) {
                item {
                    Text("🎓 Today's Attendance",
                        fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                        color = LGSecondary)
                }
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = LGSecondary
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            todayStudents.forEach { student ->
                                val attendance = todayAttendance[student.id]
                                DashboardAttendanceRow(
                                    student = student,
                                    attendance = attendance,
                                    onMarkPresent = { note ->
                                        viewModel.markAttendance(student.id, "PRESENT", note)
                                    },
                                    onMarkAbsent = { note ->
                                        viewModel.markAttendance(student.id, "ABSENT", note)
                                    }
                                )
                                if (student != todayStudents.last()) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline
                                            .copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Missed tasks ─────────────────────────────
            if (missedTasks.isNotEmpty()) {
                item {
                    Text("⚠️ Missed", fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error)
                }
                items(missedTasks) { task ->
                    TaskCard(task = task,
                        onComplete = { viewModel.completeTask(task) },
                        onDelete = { viewModel.deleteTask(task) },
                        navController = navController)
                }
            }

            // ── Today's tasks ────────────────────────────
            item {
                Text("📅 Today", fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
            }
            if (todayTasks.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("No tasks for today. Tap + to add one!",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(todayTasks) { task ->
                    TaskCard(task = task,
                        onComplete = { viewModel.completeTask(task) },
                        onDelete = { viewModel.deleteTask(task) },
                        navController = navController)
                }
            }

            // ── Tomorrow ─────────────────────────────────
            item {
                Text("🌅 Tomorrow", fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary)
            }
            if (tomorrowTasks.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("No tasks for tomorrow.",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(tomorrowTasks) { task ->
                    TaskCard(task = task,
                        onComplete = { viewModel.completeTask(task) },
                        onDelete = { viewModel.deleteTask(task) },
                        navController = navController)
                }
            }

            // Add task button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(14.dp),
                            ambientColor = ZMAccentPurple.copy(alpha = 0.4f),
                            spotColor = ZMAccentPurple.copy(alpha = 0.4f)
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(ZMAccentPurple, Color(0xFF4FC3F7))
                            )
                        )
                        .clickable { navController.navigate(Screen.AddTask.route) }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+ Add New Task", color = Color.White,
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// ── Dashboard Attendance Row ──────────────────────────────
@Composable
fun DashboardAttendanceRow(
    student: Student,
    attendance: Attendance?,
    onMarkPresent: (String) -> Unit,
    onMarkAbsent: (String) -> Unit
) {
    val studentColor = try {
        Color(android.graphics.Color.parseColor(student.colorLabel))
    } catch (e: Exception) { LGPrimary }

    val initials = student.name.split(" ")
        .take(2).joinToString("") { it.first().uppercase() }

    var showNoteDialog by remember { mutableStateOf(false) }
    var pendingStatus by remember { mutableStateOf("") }

    if (showNoteDialog) {
        AttendanceNoteDialog(
            studentName = student.name,
            status = pendingStatus,
            onDismiss = { showNoteDialog = false },
            onConfirm = { note ->
                if (pendingStatus == "PRESENT") onMarkPresent(note)
                else onMarkAbsent(note)
                showNoteDialog = false
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(studentColor, studentColor.copy(alpha = 0.6f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = Color.White,
                fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        // Name + subject
        Column(modifier = Modifier.weight(1f)) {
            Text(student.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (student.subject.isNotBlank()) {
                Text(student.subject, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Attendance status or buttons
        when (attendance?.status) {
            "PRESENT" -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF34C759).copy(alpha = 0.15f))
                        .border(0.5.dp, Color(0xFF34C759), RoundedCornerShape(20.dp))
                        .clickable {
                            pendingStatus = "ABSENT"
                            showNoteDialog = true
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("✅ Present", fontSize = 12.sp,
                        color = Color(0xFF34C759), fontWeight = FontWeight.SemiBold)
                }
            }
            "ABSENT" -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFF3B30).copy(alpha = 0.15f))
                        .border(0.5.dp, Color(0xFFFF3B30), RoundedCornerShape(20.dp))
                        .clickable {
                            pendingStatus = "PRESENT"
                            showNoteDialog = true
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("❌ Absent", fontSize = 12.sp,
                        color = Color(0xFFFF3B30), fontWeight = FontWeight.SemiBold)
                }
            }
            else -> {
                // Not marked — show P/A buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF34C759).copy(alpha = 0.15f))
                            .border(0.5.dp, Color(0xFF34C759), RoundedCornerShape(20.dp))
                            .clickable {
                                pendingStatus = "PRESENT"
                                showNoteDialog = true
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("P", fontSize = 13.sp,
                            color = Color(0xFF34C759),
                            fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFF3B30).copy(alpha = 0.15f))
                            .border(0.5.dp, Color(0xFFFF3B30), RoundedCornerShape(20.dp))
                            .clickable {
                                pendingStatus = "ABSENT"
                                showNoteDialog = true
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("A", fontSize = 13.sp,
                            color = Color(0xFFFF3B30),
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Attendance Note Dialog ────────────────────────────────
@Composable
fun AttendanceNoteDialog(
    studentName: String,
    status: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var note by remember { mutableStateOf("") }
    val statusColor = if (status == "PRESENT") Color(0xFF34C759) else Color(0xFFFF3B30)
    val statusIcon = if (status == "PRESENT") "✅" else "❌"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Mark $statusIcon $status", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(studentName, fontSize = 14.sp,
                    color = statusColor, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    placeholder = {
                        Text(
                            if (status == "ABSENT") "e.g. Sick, family emergency..."
                            else "e.g. On time, late..."
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(note) },
                colors = ButtonDefaults.buttonColors(containerColor = statusColor)
            ) {
                Text("Mark $status", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Keep existing TaskCard and other composables ──────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    navController: NavController? = null
) {
    val priorityColor = when (task.priority) {
        3 -> MaterialTheme.colorScheme.error
        2 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }
    val priorityLabel = when (task.priority) {
        3 -> "High"; 2 -> "Med"; else -> "Low"
    }
    val taskColor = try {
        Color(android.graphics.Color.parseColor(task.colorLabel))
    } catch (e: Exception) { ZMAccentPurple }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> { onComplete(); false }
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val bgColor = when (direction) {
                SwipeToDismissBoxValue.StartToEnd ->
                    MaterialTheme.colorScheme.primaryContainer
                SwipeToDismissBoxValue.EndToStart ->
                    MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.CheckCircle
                SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
                else -> null
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(it, contentDescription = null,
                        tint = when (direction) {
                            SwipeToDismissBoxValue.StartToEnd ->
                                MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        })
                }
            }
        }
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController?.navigate("task_detail/${task.id}") },
            cornerRadius = 14.dp,
            glowColor = taskColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(taskColor, taskColor.copy(alpha = 0.3f))
                            )
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(onClick = onComplete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (task.isCompleted)
                            Icons.Filled.CheckCircle
                        else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = "Complete",
                        tint = if (task.isCompleted)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted)
                            TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(task.category, fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• $priorityLabel", fontSize = 11.sp, color = priorityColor)
                        if (task.hasTime) {
                            val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            Text("• ${timeFmt.format(Date(task.deadline))}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = label, fontSize = 12.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
        color = color, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun EmptyMessage(message: String) {
    Text(text = message, fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
}