package com.example.devflow.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.devflow.ui.calendar.CalendarScreen
import com.example.devflow.ui.contests.ContestsScreen
import com.example.devflow.ui.dashboard.DashboardScreen
import com.example.devflow.ui.focus.FocusScreen
import com.example.devflow.ui.settings.SettingsScreen
import com.example.devflow.ui.task.AddTaskFromContestScreen
import com.example.devflow.ui.task.AddTaskScreen
import com.example.devflow.ui.task.EditTaskScreen
import com.example.devflow.ui.task.TaskDetailScreen
import com.example.devflow.ui.task.TasksScreen
import com.example.devflow.ui.tuition.StudentDetailScreen
import com.example.devflow.ui.tuition.TuitionScreen
import com.example.devflow.ui.splash.SplashScreen

fun enterTransition() = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + fadeIn(animationSpec = tween(300))

fun exitTransition() = slideOutHorizontally(
    targetOffsetX = { -it / 3 },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + fadeOut(animationSpec = tween(300))

fun popEnterTransition() = slideInHorizontally(
    initialOffsetX = { -it / 3 },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + fadeIn(animationSpec = tween(300))

fun popExitTransition() = slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + fadeOut(animationSpec = tween(300))

// Smooth fade only — no scale jarring
fun tabEnterTransition() = fadeIn(
    animationSpec = tween(
        durationMillis = 350,
        easing = FastOutSlowInEasing
    )
)

fun tabExitTransition() = fadeOut(
    animationSpec = tween(
        durationMillis = 200,
        easing = FastOutSlowInEasing
    )
)

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = { tabEnterTransition() },
        exitTransition = { tabExitTransition() },
        popEnterTransition = { tabEnterTransition() },
        popExitTransition = { tabExitTransition() }
    ) {
        // Bottom nav tabs
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.Tasks.route)     { TasksScreen(navController) }
        composable(Screen.Contests.route)  { ContestsScreen(navController = navController) }
        composable(Screen.Tuition.route)   { TuitionScreen(navController) }
        composable(Screen.Calendar.route)  { CalendarScreen(navController) }

        composable(
            Screen.Splash.route,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }
        ) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Detail screens with slide transition
        composable(
            Screen.Focus.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { FocusScreen(navController) }

        composable(
            Screen.Settings.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { SettingsScreen(navController) }

        composable(
            Screen.AddTask.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { AddTaskScreen(navController) }

        composable(
            "student_detail/{studentId}",
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments
                ?.getString("studentId")?.toIntOrNull() ?: return@composable
            StudentDetailScreen(studentId = studentId, navController = navController)
        }

        composable(
            "add_task_cf/{contestName}/{deadline}",
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { backStackEntry ->
            val contestName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("contestName") ?: "", "UTF-8"
            )
            val deadline = backStackEntry.arguments
                ?.getString("deadline")?.toLongOrNull()
                ?: System.currentTimeMillis()
            AddTaskFromContestScreen(
                contestName = contestName,
                deadline = deadline,
                navController = navController
            )
        }

        composable(
            "task_detail/{taskId}",
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments
                ?.getString("taskId")?.toIntOrNull() ?: return@composable
            TaskDetailScreen(taskId = taskId, navController = navController)
        }

        composable(
            "edit_task/{taskId}",
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments
                ?.getString("taskId")?.toIntOrNull() ?: return@composable
            EditTaskScreen(taskId = taskId, navController = navController)
        }
    }
}