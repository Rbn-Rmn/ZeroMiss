package com.example.devflow.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.devflow.ui.components.AppTopBar
import com.example.devflow.ui.components.GlassCard
import com.example.devflow.ui.dashboard.TaskCard
import com.example.devflow.ui.theme.LGPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val allTasks by viewModel.allTasks.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Work", "Study", "CP", "Meeting", "Personal")
    val filteredTasks = if (selectedCategory == "All") allTasks
    else allTasks.filter { it.category == selectedCategory }

    val pendingTasks = filteredTasks.filter { !it.isCompleted }
    val completedTasks = filteredTasks.filter { it.isCompleted }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            AppTopBar(title = "My Tasks", navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_task") },
                modifier = Modifier.padding(bottom = 80.dp),
                containerColor = LGPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task",
                    tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category filter
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                edgePadding = 16.dp,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = LGPrimary,
                divider = {}
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = {
                            Text(category, fontSize = 13.sp,
                                fontWeight = if (selectedCategory == category)
                                    FontWeight.SemiBold else FontWeight.Normal)
                        }
                    )
                }
            }

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No tasks here", fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap + to add a task", fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pendingTasks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Pending (${pendingTasks.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LGPrimary
                            )
                        }
                        items(pendingTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onComplete = { viewModel.completeTask(task) },
                                onDelete = { viewModel.deleteTask(task) },
                                navController = navController
                            )
                        }
                    }
                    if (completedTasks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Completed (${completedTasks.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        items(completedTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onComplete = { viewModel.completeTask(task) },
                                onDelete = { viewModel.deleteTask(task) },
                                navController = navController
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }
            }
        }
    }
}