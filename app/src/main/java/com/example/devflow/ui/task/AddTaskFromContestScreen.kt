package com.example.devflow.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.devflow.data.model.Task
import java.text.SimpleDateFormat
import androidx.compose.ui.unit.sp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskFromContestScreen(
    contestName: String,
    deadline: Long,
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    // Pre-filled with contest info
    var title by remember { mutableStateOf("Attend: $contestName") }
    var description by remember { mutableStateOf("Codeforces Contest") }
    var notes by remember { mutableStateOf("") }

    val dateFormatted = SimpleDateFormat(
        "EEE, MMM d yyyy  hh:mm a", Locale.getDefault()
    ).format(Date(deadline))

    val selectedReminders = remember { mutableStateListOf<Long>(30L, 60L) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Contest as Task", fontWeight = FontWeight.Bold) },
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

            // Contest info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Contest Details",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        contestName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "📅 $dateFormatted",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Reminders info
            SectionLabel("Reminders (pre-selected)")
            Text(
                "30 min before and 1 hour before are set by default",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    val task = Task(
                        title = title.trim(),
                        description = description.trim(),
                        notes = notes.trim(),
                        deadline = deadline,
                        hasTime = true,
                        priority = 3,
                        category = "CP",
                        colorLabel = "#1E88E5",
                        reminderOffsets = selectedReminders.joinToString(",")
                    )
                    viewModel.addTask(task)
                    navController.popBackStack()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Contest Task")
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}