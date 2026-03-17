package com.example.devflow.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devflow.ui.theme.*

// ── Liquid Glass Card ─────────────────────────────────────
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    glowColor: Color = LGPrimary,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance()
    val glassColor = if (isDark)
        Color(0xCC1C1C1E)
    else
        Color(0xCCFFFFFF)

    val borderTop = if (isDark)
        Color(0x40FFFFFF)
    else
        Color(0x80FFFFFF)

    val borderBottom = if (isDark)
        Color(0x10FFFFFF)
    else
        Color(0x20000000)

    Column(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = glowColor.copy(alpha = 0.08f),
                spotColor = glowColor.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(glassColor)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(borderTop, borderBottom)
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

// ── Gradient Stat Card with breathing animation ───────────
@Composable
fun GradientStatCard(
    label: String,
    value: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = gradientColors.first().copy(alpha = glowAlpha),
                spotColor = gradientColors.last().copy(alpha = glowAlpha)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Glowing accent border card ────────────────────────────
@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    accentColor: Color = LGPrimary,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_border")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_glow"
    )

    val isDark = MaterialTheme.colorScheme.background.luminance()
    val glassColor = if (isDark) Color(0xCC1C1C1E) else Color(0xCCFFFFFF)

    Column(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = accentColor.copy(alpha = 0.2f),
                spotColor = accentColor.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(glassColor)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = glowAlpha),
                        accentColor.copy(alpha = glowAlpha * 0.3f),
                        accentColor.copy(alpha = glowAlpha)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

// ── Liquid mesh background ────────────────────────────────
@Composable
fun LiquidMeshBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mesh1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mesh2"
    )

    val isDark = MaterialTheme.colorScheme.background.luminance()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                if (isDark) {
                    // Dark mesh — subtle glows
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MeshPurple.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.7f,
                            center = Offset(
                                size.width * 0.2f,
                                size.height * 0.15f * offset1
                            )
                        ),
                        radius = size.width * 0.7f,
                        center = Offset(size.width * 0.2f, size.height * 0.15f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MeshBlue.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.6f
                        ),
                        radius = size.width * 0.6f,
                        center = Offset(
                            size.width * (0.7f + 0.2f * offset2),
                            size.height * 0.4f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MeshTeal.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.5f
                        ),
                        radius = size.width * 0.5f,
                        center = Offset(
                            size.width * 0.4f,
                            size.height * (0.7f + 0.1f * offset1)
                        )
                    )
                } else {
                    // Light mesh — soft pastels
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MeshBlue.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.8f,
                            center = Offset(size.width * 0.1f, size.height * 0.1f)
                        ),
                        radius = size.width * 0.8f,
                        center = Offset(
                            size.width * 0.1f,
                            size.height * (0.1f + 0.05f * offset1)
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MeshPurple.copy(alpha = 0.07f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.7f
                        ),
                        radius = size.width * 0.7f,
                        center = Offset(
                            size.width * (0.8f + 0.1f * offset2),
                            size.height * 0.25f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MeshPink.copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.6f
                        ),
                        radius = size.width * 0.6f,
                        center = Offset(
                            size.width * 0.5f,
                            size.height * (0.6f + 0.08f * offset1)
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MeshGreen.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.5f
                        ),
                        radius = size.width * 0.5f,
                        center = Offset(
                            size.width * 0.2f,
                            size.height * (0.75f + 0.05f * offset2)
                        )
                    )
                }
            },
        content = content
    )
}

// ── Frosted glass top bar modifier ───────────────────────
fun Modifier.frostedGlassTopBar(isDark: Boolean): Modifier = this
    .background(
        brush = Brush.verticalGradient(
            colors = if (isDark) listOf(
                Color(0xE61C1C1E),
                Color(0xCC1C1C1E),
                Color(0x001C1C1E)
            ) else listOf(
                Color(0xF0F2F2F7),
                Color(0xCCF2F2F7),
                Color(0x00F2F2F7)
            )
        )
    )

// Extension to check if color is dark
fun Color.luminance(): Boolean {
    val r = red * 0.299f
    val g = green * 0.587f
    val b = blue * 0.114f
    return (r + g + b) < 0.5f
}

// Easing
val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)