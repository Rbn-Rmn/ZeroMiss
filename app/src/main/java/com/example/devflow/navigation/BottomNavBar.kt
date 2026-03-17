package com.example.devflow.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.devflow.ui.components.EaseInOutSine
import com.example.devflow.ui.components.luminance
import com.example.devflow.ui.theme.*

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Tasks",    Screen.Tasks.route,     Icons.Filled.CheckCircle),
        BottomNavItem("CF",       Screen.Contests.route,  Icons.Filled.Code),
        BottomNavItem("Home",     Screen.Dashboard.route, Icons.Filled.Home),
        BottomNavItem("Tuition",  Screen.Tuition.route,   Icons.Filled.School),
        BottomNavItem("Calendar", Screen.Calendar.route,  Icons.Filled.CalendarMonth),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isDark = MaterialTheme.colorScheme.background.luminance()

    val navGlassColor = if (isDark) Color(0xE6000000) else Color(0xE6FFFFFF)
    val navBorderColor = if (isDark) Color(0x30FFFFFF) else Color(0x40000000)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp, top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 32.dp,
                    shape = RoundedCornerShape(50.dp),
                    ambientColor = LGPrimary.copy(alpha = 0.15f),
                    spotColor = LGPrimary.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(50.dp))
                .background(navGlassColor)
                .border(
                    width = 0.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            navBorderColor,
                            navBorderColor.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(50.dp)
                )
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                            && currentRoute != null
                    LiquidNavItem(
                        item = item,
                        isSelected = isSelected,
                        isDark = isDark,
                        onClick = {
                            if (item.route == Screen.Dashboard.route) {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Dashboard.route) {
                                        saveState = true
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LiquidNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nav_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val selectedBg = if (isDark)
        LGDarkPrimary.copy(alpha = 0.25f)
    else
        LGPrimary.copy(alpha = 0.12f)

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) selectedBg else Color.Transparent,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "nav_bg"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) LGPrimary
        else if (isDark) Color.White.copy(alpha = 0.45f)
        else Color.Black.copy(alpha = 0.4f),
        animationSpec = tween(300),
        label = "icon_color"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .heightIn(min = 44.dp)
            .widthIn(min = 44.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(bgColor)
            .then(
                if (isSelected) Modifier.border(
                    width = 0.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            LGPrimary.copy(alpha = glowAlpha * 0.7f),
                            LGPurple.copy(alpha = glowAlpha * 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                ) else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(animationSpec = tween(150)),
                exit = shrinkHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut(animationSpec = tween(150))
            ) {
                Row {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.label,
                        color = LGPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}