package com.example.devflow.navigation

sealed class Screen(val route: String) {
    object Dashboard   : Screen("dashboard")
    object Tasks       : Screen("tasks")
    object Contests    : Screen("contests")
    object Tuition     : Screen("tuition")
    object Calendar    : Screen("calendar")
    object Focus       : Screen("focus")
    object Settings    : Screen("settings")
    object AddTask     : Screen("add_task")
    object TaskDetail  : Screen("task_detail/{taskId}")
    object EditTask    : Screen("edit_task/{taskId}")
    object Splash : Screen("splash")
}