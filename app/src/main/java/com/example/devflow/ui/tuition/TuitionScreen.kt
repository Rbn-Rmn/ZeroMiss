package com.example.devflow.ui.tuition

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.devflow.data.model.Student
import com.example.devflow.ui.components.AppTopBar
import com.example.devflow.ui.components.GlassCard
import com.example.devflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TuitionScreen(
    navController: NavController,
    viewModel: TuitionViewModel = viewModel()
) {
    val students by viewModel.allStudents.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddStudentDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { student ->
                viewModel.addStudent(student)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(title = "Tuition", navController = navController)
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .size(56.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        ambientColor = LGPrimary.copy(alpha = 0.4f),
                        spotColor = LGPrimary.copy(alpha = 0.4f)
                    )
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(LGPrimary, LGPurple)
                        )
                    )
                    .clickable { showAddDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Student",
                    tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    ) { innerPadding ->
        if (students.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎓", fontSize = 40.sp)
                        Text("No students yet",
                            fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Tap + to add your first student",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(students) { student ->
                    LiquidStudentCard(
                        student = student,
                        onClick = {
                            navController.navigate("student_detail/${student.id}")
                        },
                        onDelete = { viewModel.deleteStudent(student) }
                    )
                }
                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
        }
    }
}

@Composable
fun LiquidStudentCard(
    student: Student,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val studentColor = try {
        Color(android.graphics.Color.parseColor(student.colorLabel))
    } catch (e: Exception) { LGPrimary }

    val initials = student.name.split(" ")
        .take(2).joinToString("") { it.first().uppercase() }

    val days = student.scheduleDays.split(",")
        .filter { it.isNotBlank() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = studentColor.copy(alpha = 0.15f),
                spotColor = studentColor.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        studentColor.copy(alpha = 0.4f),
                        studentColor.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar with gradient
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = studentColor.copy(alpha = 0.3f),
                        spotColor = studentColor.copy(alpha = 0.3f)
                    )
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                studentColor,
                                studentColor.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = Color.White,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(student.name, fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold)
                if (student.subject.isNotBlank()) {
                    Text(student.subject, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (student.grade.isNotBlank()) {
                    Text("Class: ${student.grade}", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (days.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        days.take(5).forEach { day ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(studentColor.copy(alpha = 0.15f))
                                    .border(
                                        0.5.dp, studentColor.copy(alpha = 0.4f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(day.trim(), fontSize = 9.sp,
                                    color = studentColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onAdd: (Student) -> Unit
) {
    val colorOptions = listOf(
        "#007AFF", "#5856D6", "#34C759",
        "#FF9500", "#FF3B30", "#00ACC1", "#FF2D55"
    )
    val allDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    var name by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var guardianPhone by remember { mutableStateOf("") }
    var selectedDays = remember { mutableStateListOf<String>() }
    var selectedColor by remember { mutableStateOf("#007AFF") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Student", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Name *") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Class/Grade") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = guardianPhone,
                    onValueChange = { guardianPhone = it },
                    label = { Text("Guardian Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Schedule Days", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    allDays.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) LGPrimary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) LGPrimary else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (isSelected) selectedDays.remove(day)
                                    else selectedDays.add(day)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(day, fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) LGPrimary
                                else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Text("Color", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (selectedColor == hex)
                                        Modifier.border(3.dp,
                                            MaterialTheme.colorScheme.onBackground,
                                            CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(LGPrimary, LGPurple)
                        )
                    )
                    .clickable {
                        if (name.isBlank()) { nameError = true; return@clickable }
                        onAdd(Student(
                            name = name.trim(),
                            subject = subject.trim(),
                            grade = grade.trim(),
                            guardianPhone = guardianPhone.trim(),
                            scheduleDays = selectedDays.joinToString(","),
                            colorLabel = selectedColor,
                            notes = "",
                            createdAt = System.currentTimeMillis()
                        ))
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Add", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun StudentCard(
    student: Student,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    LiquidStudentCard(student = student, onClick = onClick, onDelete = onDelete)
}