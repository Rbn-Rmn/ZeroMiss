package com.example.devflow.ui.task

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.devflow.ReminderScheduler
import com.example.devflow.AlarmScheduler
import com.example.devflow.data.model.Task
import com.example.devflow.ui.components.GlassCard
import com.example.devflow.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTaskScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Work") }
    var selectedPriority by remember { mutableIntStateOf(2) }

    val colorOptions = listOf(
        "#007AFF", "#5856D6", "#34C759",
        "#FF9500", "#FF3B30", "#00ACC1", "#FF2D55"
    )
    var selectedColor by remember { mutableStateOf("#007AFF") }

    var selectedDeadline by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    var showDatePicker by remember { mutableStateOf(false) }
    var hasTime by remember { mutableStateOf(false) }
    var timeHour by remember { mutableIntStateOf(9) }
    var timeMinute by remember { mutableIntStateOf(0) }
    var selectedRepeat by remember { mutableStateOf("none") }
    val selectedReminders = remember { mutableStateListOf<Long>() }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDeadline = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

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
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Text("New Task", fontWeight = FontWeight.Bold, fontSize = 28.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back",
                                modifier = Modifier.size(20.dp))
                        }
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

            // Title field
            GlassCard(modifier = Modifier.fillMaxWidth(), glowColor = if (titleError) LGError else LGPrimary) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = titleError,
                    supportingText = { if (titleError) Text("Title cannot be empty") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent
                    )
                )
            }

            // Description
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            // Notes
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Checklist (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 6,
                    placeholder = { Text("- item 1\n- item 2") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            // Category
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Work", "Study", "CP", "Meeting", "Personal").forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected)
                                            Brush.linearGradient(listOf(LGPrimary, LGPurple))
                                        else
                                            Brush.linearGradient(listOf(
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                            ))
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(cat, fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White
                                    else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // Priority
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Priority", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            1 to Pair("Low", LGSecondary),
                            2 to Pair("Medium", LGTertiary),
                            3 to Pair("High", LGError)
                        ).forEach { (value, pair) ->
                            val (label, color) = pair
                            val isSelected = selectedPriority == value
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) color.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) color else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedPriority = value }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(label, fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) color
                                    else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // Color Label
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Color Label", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        colorOptions.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (selectedColor == hex)
                                            Modifier.border(3.dp,
                                                MaterialTheme.colorScheme.onBackground,
                                                CircleShape)
                                        else
                                            Modifier.border(2.dp,
                                                color.copy(alpha = 0.3f), CircleShape)
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }
            }

            // Deadline Date
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                glowColor = LGPrimary
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null,
                        tint = LGPrimary, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Deadline", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val formatted = SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault())
                            .format(Date(selectedDeadline))
                        Text(formatted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp))
                }
            }

            // Time
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { timePickerDialog.show() },
                glowColor = if (hasTime) LGPrimary else MaterialTheme.colorScheme.outline
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.Schedule, contentDescription = null,
                        tint = if (hasTime) LGPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Time (optional)", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (hasTime) {
                            val amPm = if (timeHour < 12) "AM" else "PM"
                            val displayHour = if (timeHour % 12 == 0) 12 else timeHour % 12
                            Text("%d:%02d %s".format(displayHour, timeMinute, amPm),
                                fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                color = LGPrimary)
                        } else {
                            Text("Tap to set time", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (hasTime) {
                        TextButton(
                            onClick = { hasTime = false },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) { Text("Clear", fontSize = 12.sp, color = LGError) }
                    } else {
                        Icon(Icons.Filled.ChevronRight, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (!hasTime) {
                Text("No time set — reminders will be disabled",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp))
            }

            // Reminders
            if (hasTime) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Reminders", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ReminderScheduler.REMINDER_PRESETS.forEach { (offsetMins, label) ->
                                val isSelected = selectedReminders.contains(offsetMins)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected)
                                                Brush.linearGradient(listOf(LGPrimary, LGPurple))
                                            else
                                                Brush.linearGradient(listOf(
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                                ))
                                        )
                                        .clickable {
                                            if (isSelected) selectedReminders.remove(offsetMins)
                                            else selectedReminders.add(offsetMins)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Text(label, fontSize = 12.sp,
                                        color = if (isSelected) Color.White
                                        else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }

            // Repeat
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Repeat", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("none", "daily", "weekly").forEach { repeat ->
                            val isSelected = selectedRepeat == repeat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected)
                                            Brush.linearGradient(listOf(LGPrimary, LGPurple))
                                        else
                                            Brush.linearGradient(listOf(
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                            ))
                                    )
                                    .clickable { selectedRepeat = repeat }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(repeat.replaceFirstChar { it.uppercase() },
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White
                                    else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = LGPrimary.copy(alpha = 0.4f),
                        spotColor = LGPrimary.copy(alpha = 0.4f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(LGPrimary, LGPurple)
                        )
                    )
                    .clickable {
                        if (title.isBlank()) { titleError = true; return@clickable }
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = selectedDeadline
                            if (hasTime) {
                                set(Calendar.HOUR_OF_DAY, timeHour)
                                set(Calendar.MINUTE, timeMinute)
                                set(Calendar.SECOND, 0)
                            }
                        }
                        val task = Task(
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
                        viewModel.addTask(task)
                        AlarmScheduler.scheduleTaskReminders(context, task)
                        navController.popBackStack()
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Save Task", color = Color.White,
                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text = text, fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}