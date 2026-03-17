package com.example.devflow.ui.focus

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    navController: NavController,
    viewModel: FocusViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60
    val progress = state.remainingSeconds.toFloat() / state.totalSeconds.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "timer_progress"
    )

    // Customize dialog state
    var showCustomDialog by remember { mutableStateOf(false) }

    // Audio file picker
    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment
                ?.substringAfterLast("/") ?: "audio file"
            viewModel.setAudio(context, uri, fileName)
        }
    }

    // Customize durations dialog
    if (showCustomDialog) {
        CustomizeDurationsDialog(
            state = state,
            onDismiss = { showCustomDialog = false },
            onSaveWork = { viewModel.saveWorkDuration(it) },
            onSaveShortBreak = { viewModel.saveBreakDuration(true, it) },
            onSaveLongBreak = { viewModel.saveBreakDuration(false, it) }
        )
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                title = { Text("Focus Timer", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { showCustomDialog = true }) {
                        Text("Customize")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Mode selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    TimerMode.WORK to "Work",
                    TimerMode.SHORT_BREAK to "Short",
                    TimerMode.LONG_BREAK to "Long"
                ).forEach { (mode, label) ->
                    FilterChip(
                        selected = state.mode == mode,
                        onClick = { viewModel.setMode(mode, context) },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            // Circular timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = when (state.mode) {
                        TimerMode.WORK        -> MaterialTheme.colorScheme.primary
                        TimerMode.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
                        TimerMode.LONG_BREAK  -> MaterialTheme.colorScheme.tertiary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%02d:%02d".format(minutes, seconds),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = when (state.mode) {
                            TimerMode.WORK        -> "Focus"
                            TimerMode.SHORT_BREAK -> "Short Break"
                            TimerMode.LONG_BREAK  -> "Long Break"
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick duration slider
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val sliderMinutes = (state.totalSeconds / 60).toFloat()
                var sliderValue by remember(state.totalSeconds) {
                    mutableFloatStateOf(sliderMinutes)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Duration",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${sliderValue.toInt()} min",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = {
                        viewModel.setCustomDuration(sliderValue.toInt())
                    },
                    valueRange = 1f..120f,
                    steps = 0,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1m", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("120m", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Start / Reset buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.startStop(context) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (state.isRunning) "Pause" else "Start",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.reset(context) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset", fontSize = 16.sp)
                }
            }

            HorizontalDivider()

            // Audio section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Focus Audio",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { audioPicker.launch("audio/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.AudioFile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Audio File", fontSize = 13.sp)
                    }
                    if (state.audioUri != null) {
                        IconButton(onClick = { viewModel.clearAudio(context) }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Remove audio",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Text(
                    text = if (state.audioUri != null)
                        "🎵  ${state.audioFileName}"
                    else
                        "No audio — timer runs silently",
                    fontSize = 12.sp,
                    color = if (state.audioUri != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (state.audioUri != null) {
                    Text(
                        "Audio will loop while timer is running",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatFocusCard(
                    label = "Sessions",
                    value = state.sessionsCompleted.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatFocusCard(
                    label = "Focus time",
                    value = "${state.todayFocusMinutes}m",
                    modifier = Modifier.weight(1f)
                )
                StatFocusCard(
                    label = "Next break",
                    value = "${4 - (state.sessionsCompleted % 4)} left",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun CustomizeDurationsDialog(
    state: FocusState,
    onDismiss: () -> Unit,
    onSaveWork: (Int) -> Unit,
    onSaveShortBreak: (Int) -> Unit,
    onSaveLongBreak: (Int) -> Unit
) {
    var workInput by remember {
        mutableStateOf((state.workSeconds / 60).toString())
    }
    var shortInput by remember {
        mutableStateOf((state.shortBreakSeconds / 60).toString())
    }
    var longInput by remember {
        mutableStateOf((state.longBreakSeconds / 60).toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customize Durations", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DurationInputRow(
                    label = "Work session",
                    value = workInput,
                    onValueChange = { workInput = it }
                )
                DurationInputRow(
                    label = "Short break",
                    value = shortInput,
                    onValueChange = { shortInput = it }
                )
                DurationInputRow(
                    label = "Long break",
                    value = longInput,
                    onValueChange = { longInput = it }
                )
                Text(
                    "Enter duration in minutes",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                workInput.toIntOrNull()?.let { onSaveWork(it.coerceIn(1, 120)) }
                shortInput.toIntOrNull()?.let { onSaveShortBreak(it.coerceIn(1, 60)) }
                longInput.toIntOrNull()?.let { onSaveLongBreak(it.coerceIn(1, 60)) }
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DurationInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 3) onValueChange(it) },
            modifier = Modifier.width(80.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            suffix = { Text("min", fontSize = 11.sp) }
        )
    }
}

@Composable
fun StatFocusCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}