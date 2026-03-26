package com.lmen918.pda.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun ColorWheelPicker(
    initialColor: Color = Color.Red,
    onColorSelected: (Color) -> Unit
) {
    val initialHsv = colorToHsv(initialColor)
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    var center by remember { mutableStateOf(Offset.Zero) }
    var outerRadius by remember { mutableFloatStateOf(0f) }
    var ringWidth by remember { mutableFloatStateOf(0f) }
    var innerRadius by remember { mutableFloatStateOf(0f) }
    var squareSize by remember { mutableFloatStateOf(0f) }
    var squareOffset by remember { mutableStateOf(Offset.Zero) }

    var isDraggingRing by remember { mutableStateOf(false) }
    var isDraggingSV by remember { mutableStateOf(false) }

    val selectedColor = remember(hue, saturation, value) {
        Color.hsv(hue, saturation, value)
    }

    LaunchedEffect(selectedColor) {
        onColorSelected(selectedColor)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist in (innerRadius)..(outerRadius)) {
                            val angle = (atan2(dy, dx) * 180f / PI.toFloat() + 360f) % 360f
                            hue = angle
                        } else if (
                            offset.x >= squareOffset.x && offset.x <= squareOffset.x + squareSize &&
                            offset.y >= squareOffset.y && offset.y <= squareOffset.y + squareSize
                        ) {
                            saturation = ((offset.x - squareOffset.x) / squareSize).coerceIn(0f, 1f)
                            value = (1f - (offset.y - squareOffset.y) / squareSize).coerceIn(0f, 1f)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val dist = sqrt(dx * dx + dy * dy)
                            isDraggingRing = dist in (innerRadius)..(outerRadius)
                            isDraggingSV = !isDraggingRing &&
                                    offset.x >= squareOffset.x && offset.x <= squareOffset.x + squareSize &&
                                    offset.y >= squareOffset.y && offset.y <= squareOffset.y + squareSize
                        },
                        onDrag = { change, _ ->
                            val offset = change.position
                            if (isDraggingRing) {
                                val dx = offset.x - center.x
                                val dy = offset.y - center.y
                                val angle = (atan2(dy, dx) * 180f / PI.toFloat() + 360f) % 360f
                                hue = angle
                            } else if (isDraggingSV) {
                                saturation = ((offset.x - squareOffset.x) / squareSize).coerceIn(0f, 1f)
                                value = (1f - (offset.y - squareOffset.y) / squareSize).coerceIn(0f, 1f)
                            }
                        },
                        onDragEnd = {
                            isDraggingRing = false
                            isDraggingSV = false
                        }
                    )
                }
        ) {
            val size = minOf(this.size.width, this.size.height)
            center = Offset(this.size.width / 2f, this.size.height / 2f)
            outerRadius = size / 2f
            ringWidth = size * 0.1f
            innerRadius = outerRadius - ringWidth
            val svSize = innerRadius * 2f / sqrt(2f) * 0.9f
            squareSize = svSize
            squareOffset = Offset(center.x - svSize / 2f, center.y - svSize / 2f)

            val hueColors = (0..360 step 6).map { h ->
                Color.hsv(h.toFloat(), 1f, 1f)
            }.toMutableList().also { it.add(it.first()) }

            drawCircle(
                brush = Brush.sweepGradient(hueColors, center = center),
                radius = (outerRadius + innerRadius) / 2f,
                center = center,
                style = Stroke(width = ringWidth)
            )

            val hueColor = Color.hsv(hue, 1f, 1f)
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White, hueColor),
                    startX = squareOffset.x,
                    endX = squareOffset.x + svSize
                ),
                topLeft = squareOffset,
                size = Size(svSize, svSize)
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = squareOffset.y,
                    endY = squareOffset.y + svSize
                ),
                topLeft = squareOffset,
                size = Size(svSize, svSize)
            )

            val hueAngleRad = hue * PI.toFloat() / 180f
            val indicatorRadius = (outerRadius + innerRadius) / 2f
            val indicatorX = center.x + indicatorRadius * cos(hueAngleRad)
            val indicatorY = center.y + indicatorRadius * sin(hueAngleRad)
            drawCircle(color = Color.White, radius = ringWidth / 2f * 0.8f, center = Offset(indicatorX, indicatorY))
            drawCircle(color = Color.Black, radius = ringWidth / 2f * 0.8f, center = Offset(indicatorX, indicatorY),
                style = Stroke(width = 2f))

            val svX = squareOffset.x + saturation * svSize
            val svY = squareOffset.y + (1f - value) * svSize
            drawCircle(color = Color.White, radius = 8f, center = Offset(svX, svY))
            drawCircle(color = Color.Black, radius = 8f, center = Offset(svX, svY),
                style = Stroke(width = 2f))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Canvas(modifier = Modifier.size(48.dp, 24.dp)) {
            drawRect(color = selectedColor)
            drawRect(color = Color.Gray, style = Stroke(width = 2f))
        }
    }
}

fun colorToHsv(color: Color): FloatArray {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    val h = when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6)
        max == g -> 60f * (((b - r) / delta) + 2)
        else -> 60f * (((r - g) / delta) + 4)
    }.let { if (it < 0) it + 360f else it }

    val s = if (max == 0f) 0f else delta / max
    val v = max

    return floatArrayOf(h, s, v)
}
