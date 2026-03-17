package com.example.devflow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.devflow.navigation.BottomNavBar
import com.example.devflow.navigation.NavGraph
import com.example.devflow.navigation.Screen
import com.example.devflow.ui.components.LiquidMeshBackground
import com.example.devflow.ui.theme.DevFlowTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val isDarkMode by ThemeManager.isDarkMode(context)
                .collectAsStateWithLifecycle(initialValue = false)
            DevFlowTheme(darkTheme = isDarkMode) {
                DevFlowApp()
            }
        }
    }
}

@Composable
fun DevFlowApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabRoutes = listOf(
        Screen.Tasks.route,
        Screen.Contests.route,
        Screen.Dashboard.route,
        Screen.Tuition.route,
        Screen.Calendar.route
    )

    val isOnTab = currentRoute in tabRoutes.toSet()
            && currentRoute != Screen.Splash.route

    LiquidMeshBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (currentRoute != Screen.Splash.route) {
                    BottomNavBar(navController)
                }
            }
        ) { innerPadding ->
            val modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = 0.dp
            )

            if (isOnTab) {
                val currentIndex = tabRoutes.indexOf(currentRoute).takeIf { it >= 0 } ?: 2
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .pointerInput(currentRoute) {
                            var totalDrag = 0f
                            var hapticFired = false
                            var isHorizontal: Boolean? = null

                            detectHorizontalDragGestures(
                                onDragStart = {
                                    totalDrag = 0f
                                    hapticFired = false
                                    isHorizontal = null
                                },
                                onDragEnd = {
                                    if (isHorizontal == true) {
                                        if (totalDrag < -120f && currentIndex < tabRoutes.size - 1) {
                                            navController.navigate(tabRoutes[currentIndex + 1]) {
                                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        } else if (totalDrag > 120f && currentIndex > 0) {
                                            navController.navigate(tabRoutes[currentIndex - 1]) {
                                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    totalDrag += dragAmount
                                    if (!hapticFired && (totalDrag > 80f || totalDrag < -80f)) {
                                        hapticFired = true
                                        isHorizontal = true
                                        triggerHaptic(context)
                                    }
                                }
                            )
                        }
                ) {
                    NavGraph(navController = navController)
                }
            } else {
                NavGraph(navController = navController, modifier = modifier)
            }
        }
    }
}

suspend fun PointerInputScope.detectHorizontalDragGesture(
    tabRoutes: List<String>,
    currentIndex: Int,
    navController: NavController,
    context: Context
) {
    var totalDrag = 0f
    var hapticFired = false
    var isHorizontalSwipe: Boolean? = null

    detectHorizontalDragGestures(
        onDragStart = {
            totalDrag = 0f
            hapticFired = false
            isHorizontalSwipe = null
        },
        onDragEnd = {
            if (isHorizontalSwipe == true) {
                if (totalDrag < -120f && currentIndex < tabRoutes.size - 1) {
                    navController.navigate(tabRoutes[currentIndex + 1]) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                } else if (totalDrag > 120f && currentIndex > 0) {
                    navController.navigate(tabRoutes[currentIndex - 1]) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
        onHorizontalDrag = { _, dragAmount ->
            totalDrag += dragAmount
            if (!hapticFired && (totalDrag > 80f || totalDrag < -80f)) {
                hapticFired = true
                isHorizontalSwipe = true
                triggerHaptic(context)
            }
        }
    )
}

fun triggerHaptic(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(
                Context.VIBRATOR_MANAGER_SERVICE
            ) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(18)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}