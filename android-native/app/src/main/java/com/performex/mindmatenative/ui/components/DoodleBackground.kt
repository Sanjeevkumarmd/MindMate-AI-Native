package com.performex.mindmatenative.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.random.Random

@Composable
fun DoodleBackground(
    modifier: Modifier = Modifier,
    doodleColor: Color = Color.White.copy(alpha = 0.05f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DoodleAnimation")
    
    // Create random doodles once
    val doodles = remember {
        List(15) {
            DoodleState(
                icon = getRandomStudyIcon(),
                initialOffset = Offset(
                    x = Random.nextFloat(),
                    y = Random.nextFloat()
                ),
                speed = Random.nextFloat() * 0.05f + 0.02f,
                rotationSpeed = Random.nextFloat() * 20f + 10f,
                scaleSpeed = Random.nextFloat() * 0.5f + 0.5f
            )
        }
    }

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlobalProgress"
    )

    val painters = doodles.map { rememberVectorPainter(it.icon) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        doodles.forEachIndexed { index, doodle ->
            val painter = painters[index]
            
            // Calculate current position based on progress and speed
            val currentY = (doodle.initialOffset.y + animationProgress * doodle.speed) % 1f
            val xPos = doodle.initialOffset.x * width
            val yPos = currentY * height
            
            val rotation = (animationProgress * doodle.rotationSpeed * 360f) % 360f
            val scale = 0.8f + 0.2f * kotlin.math.sin(animationProgress * doodle.scaleSpeed * 2 * kotlin.math.PI.toFloat())

            withTransform({
                translate(left = xPos, top = yPos)
                rotate(degrees = rotation)
                scale(scaleX = scale, scaleY = scale)
            }) {
                with(painter) {
                    draw(
                        size = androidx.compose.ui.geometry.Size(40.dp.toPx(), 40.dp.toPx()),
                        alpha = 0.08f
                    )
                }
            }
        }
    }
}

private data class DoodleState(
    val icon: ImageVector,
    val initialOffset: Offset,
    val speed: Float,
    val rotationSpeed: Float,
    val scaleSpeed: Float
)

private fun getRandomStudyIcon(): ImageVector {
    val icons = listOf(
        Icons.Outlined.Edit,
        Icons.Outlined.Book,
        Icons.Outlined.Lightbulb,
        Icons.Outlined.Psychology,
        Icons.Outlined.School,
        Icons.Outlined.Calculate,
        Icons.Outlined.AutoStories,
        Icons.Outlined.HistoryEdu,
        Icons.Outlined.ModeEdit,
        Icons.Outlined.Timer
    )
    return icons.random()
}
