package com.example.devflow.ui.task

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.devflow.AlarmScheduler
import com.example.devflow.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditTaskScreen(
    taskId: Int,
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val context = LocalContext.current
    val allTasks by viewModel.allTasks.collectAsState()
    val task = allTasks.find { it.id == taskId }

    if (task == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Task not found")
        }
        return
    }

    // Pre-fill all fields with existing task data
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var notes by remember { mutableStateOf(task.notes) }
    var titleError by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(task.category) }
    var selectedPriority by remember { mutableIntStateOf(task.priority) }
    var selectedColor by remember { mutableStateOf(task.colorLabel) }
    var selectedDeadline by remember { mutableLongStateOf(task.deadline) }
    var selectedRepeat by remember { mutableStateOf(task.repeatType) }
    var hasTime by remember { mutableStateOf(task.hasTime) }

    // Parse existing time from deadline
    val existingCal = Calendar.getInstance().apply { timeInMillis = task.deadline }
    var timeHour by remember { mutableIntStateOf(existingCal.get(Calendar.HOUR_OF_DAY)) }
    var timeMinute by remember { mutableIntStateOf(existingCal.get(Calendar.MINUTE)) }

    // Parse existing reminders
    val selectedReminders = remember {
        mutableStateListOf<Long>().also { list ->
            task.reminderOffsets.split(",")
                .mapNotNull { it.trim().toLongOrNull() }
                .forEach { list.add(it) }
        }
    }

    val colorOptions = listOf(
        "#6650A4", "#E53935", "#43A047",
        "#FB8C00", "#1E88E5", "#00ACC1", "#8E24AA"
    )

    // Date picker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = task.deadline
    )
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDeadline = it
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // Time picker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            timeHour = hour
            timeMinute = minute
            hasTime = true
        },
        timeHour, timeMinute, false
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Title ──────────────────────────────────────
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("Task Title *") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError,
                supportingText = { if (titleError) Text("Title cannot be empty") },
                singleLine = true
            )

            // ── Description ────────────────────────────────
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // ── Notes ──────────────────────────────────────
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Checklist (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 6,
                placeholder = { Text("- item 1\n- item 2\n...") }
            )

            // ── Category ───────────────────────────────────
            SectionLabel("Category")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Work", "Study", "CP", "Meeting", "Personal").forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            // ── Priority ───────────────────────────────────
            SectionLabel("Priority")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1 to "Low", 2 to "Medium", 3 to "High").forEach { (value, label) ->
                    val color = when (value) {
                        3 -> MaterialTheme.colorScheme.error
                        2 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                    FilterChip(
                        selected = selectedPriority == value,
                        onClick = { selectedPriority = value },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.2f),
                            selectedLabelColor = color
                        )
                    )
                }
            }

            // ── Color Label ────────────────────────────────
            SectionLabel("Color Label")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colorOptions.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (selectedColor == hex)
                                    Modifier.border(
                                        3.dp,
                                        MaterialTheme.colorScheme.onBackground,
                                        CircleShape
                                    )
                                else Modifier
                            )
                            .clickable { selectedColor = hex }
                    )
                }
            }

            // ── Deadline Date ──────────────────────────────
            SectionLabel("Deadline Date")
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                val formatted = SimpleDateFormat(
                    "EEE, MMM d yyyy", Locale.getDefault()
                ).format(Date(selectedDeadline))
                Text("📅  $formatted")
            }

            // ── Time (optional) ────────────────────────────
            SectionLabel("Time (optional)")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.weight(1f)
                ) {
                    if (hasTime) {
                        val amPm = if (timeHour < 12) "AM" else "PM"
                        val displayHour = if (timeHour % 12 == 0) 12 else timeHour % 12
                        Text("🕐  %d:%02d %s".format(displayHour, timeMinute, amPm))
                    } else {
                        Text("🕐  Set time")
                    }
                }
                if (hasTime) {
                    TextButton(onClick = {
                        hasTime = false
                        selectedReminders.clear()
                    }) { Text("Clear") }
                }
            }
            if (!hasTime) {
                Text(
                    "No time set — reminders will be disabled",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Reminders ──────────────────────────────────
            if (hasTime) {
                SectionLabel("Reminders (select multiple)")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReminderScheduler.REMINDER_PRESETS.forEach { (offsetMins, label) ->
                        FilterChip(
                            selected = selectedReminders.contains(offsetMins),
                            onClick = {
                                if (selectedReminders.contains(offsetMins))
                                    selectedReminders.remove(offsetMins)
                                else
                                    selectedReminders.add(offsetMins)
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // ── Repeat ─────────────────────────────────────
            SectionLabel("Repeat")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("none", "daily", "weekly").forEach { repeat ->
                    FilterChip(
                        selected = selectedRepeat == repeat,
                        onClick = { selectedRepeat = repeat },
                        label = { Text(repeat.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Save Button ────────────────────────────────
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }

                    val cal = Calendar.getInstance().apply {
                        timeInMillis = selectedDeadline
                        if (hasTime) {
                            set(Calendar.HOUR_OF_DAY, timeHour)
                            set(Calendar.MINUTE, timeMinute)
                            set(Calendar.SECOND, 0)
                        }
                    }

                    val updatedTask = task.copy(
                        title = title.trim(),
                        description = description.trim(),
                        notes = notes.trim(),
                        deadline = cal.timeInMillis,
                        hasTime = hasTime,
                        priority = selectedPriority,
                        category = selectedCategory,
                        colorLabel = selectedColor,
                        repeatType = selectedRepeat,
                        reminderOffsets = selectedReminders.joinToString(",")
                    )

                    viewModel.updateTask(updatedTask)
                    AlarmScheduler.scheduleTaskReminders(context, updatedTask)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}