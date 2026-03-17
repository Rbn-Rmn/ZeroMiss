package com.example.devflow.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.devflow.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    navController: NavController,
    extraActions: @Composable () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        title = {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        },
        actions = {
            extraActions()
            // Focus Timer — always visible
            IconButton(onClick = {
                navController.navigate(Screen.Focus.route)
            }) {
                Icon(Icons.Filled.Timer, contentDescription = "Focus Timer")
            }
            // Settings — always visible
            IconButton(onClick = {
                navController.navigate(Screen.Settings.route)
            }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
    )
}