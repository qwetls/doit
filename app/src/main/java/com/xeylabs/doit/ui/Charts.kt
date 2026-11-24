package com.xeylabs.doit.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.ui.theme.VioletAccent
import com.xeylabs.doit.viewmodel.CategorySpend
import com.xeylabs.doit.viewmodel.DaySpend
import com.xeylabs.doit.viewmodel.MonthSpend

data class BarEntry(val label: String, val amount: Long)

fun List<DaySpend>.toBars(): List<BarEntry> = map { BarEntry(it.label, it.amount) }
fun List<MonthSpend>.toBars(): List<BarEntry> = map { BarEntry(it.label, it.amount) }

@Composable
fun BarChart(
    bars: List<BarEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = VioletAccent,
    height: Dp = 160.dp
) {
    val max = (bars.maxOfOrNull { it.amount } ?: 0L).coerceAtLeast(1L)
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val slot = size.width / bars.size
        val barWidth = slot * 0.55f
        val baselineY = size.height - 4f
        drawLine(
            color = Color.White.copy(alpha = 0.12f),
            start = Offset(0f, baselineY),
            end = Offset(size.width, baselineY),
            strokeWidth = 2f
        )
        bars.forEachIndexed { index, bar ->
            val h = (size.height - 12f) * (bar.amount.toFloat() / max.toFloat())
            val isLast = index == bars.lastIndex
            val color = if (isLast) barColor else barColor.copy(alpha = 0.5f)
            val x = slot * index + (slot - barWidth) / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(x, baselineY - h),
                size = Size(barWidth, h.coerceAtLeast(if (bar.amount > 0) 6f else 0f)),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        bars.forEach { bar ->
            Text(
                text = bar.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

data class DonutSlice(val label: String, val amount: Long, val color: Color)

@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerText: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val total = slices.sumOf { it.amount }.coerceAtLeast(1L)
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 700),
        label = "donut"
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            var startAngle = -90f
            val stroke = Stroke(width = 26f, cap = StrokeCap.Butt)
            slices.forEach { slice ->
                val sweep = 360f * (slice.amount.toFloat() / total.toFloat()) * animatedProgress
                if (sweep > 3f) {
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep - 2f,
                        useCenter = false,
                        style = stroke
                    )
                }
                startAngle += 360f * (slice.amount.toFloat() / total.toFloat())
            }
        }
        Text(
            text = centerText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

fun List<CategorySpend>.toDonutSlices(): List<DonutSlice> =
    map { DonutSlice(it.category.label, it.amount, com.xeylabs.doit.ui.theme.categoryColor(it.category.name)) }

@Composable
fun BudgetRing(
    progress: Float,
    color: Color,
    centerText: String,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    strokeWidth: Dp = 10.dp
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "ring"
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = Color.White.copy(alpha = 0.10f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            if (animated > 0.01f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    style = stroke
                )
            }
        }
        Text(
            text = centerText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
