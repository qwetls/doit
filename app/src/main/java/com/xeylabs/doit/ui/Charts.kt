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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.ui.theme.Emerald
import com.xeylabs.doit.ui.theme.EmeraldAccent
import com.xeylabs.doit.ui.theme.EmeraldStrong
import com.xeylabs.doit.viewmodel.CategorySpend
import com.xeylabs.doit.viewmodel.DaySpend
import com.xeylabs.doit.viewmodel.MonthSpend

data class BarEntry(val label: String, val amount: Long)

fun List<DaySpend>.toDayBars(): List<BarEntry> = map { BarEntry(it.label, it.amount) }
fun List<MonthSpend>.toMonthBars(): List<BarEntry> = map { BarEntry(it.label, it.amount) }

@Composable
fun BarChart(
    bars: List<BarEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = EmeraldAccent,
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
    map { DonutSlice(it.category.label, it.amount, Color(it.category.colorHex)) }

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

@Composable
fun NetFlowChart(
    points: List<Long>,
    labels: List<String>,
    peakIndex: Int,
    modifier: Modifier = Modifier,
    height: Dp = 128.dp
) {
    if (points.isEmpty()) return
    val max = points.max()
    val min = points.min()
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val w = size.width
        val h = size.height
        val topPad = 10f
        val bottomPad = 6f
        val span = (max - min).toFloat().coerceAtLeast(1f)
        fun xAt(i: Int): Float = if (points.size == 1) w / 2f else w * i.toFloat() / (points.size - 1).toFloat()
        fun yAt(v: Long): Float = topPad + (h - topPad - bottomPad) * (1f - (v - min).toFloat() / span)
        val pts = points.mapIndexed { i, v -> Offset(xAt(i), yAt(v)) }
        val linePath = Path()
        linePath.moveTo(pts.first().x, pts.first().y)
        if (pts.size == 1) {
            linePath.lineTo(pts.first().x, pts.first().y)
        } else {
            for (i in 0 until pts.size - 1) {
                val p0 = pts.getOrNull(i - 1) ?: pts[i]
                val p1 = pts[i]
                val p2 = pts[i + 1]
                val p3 = pts.getOrNull(i + 2) ?: p2
                val c1x = p1.x + (p2.x - p0.x) / 6f
                val c1y = p1.y + (p2.y - p0.y) / 6f
                val c2x = p2.x - (p3.x - p1.x) / 6f
                val c2y = p2.y - (p3.y - p1.y) / 6f
                linePath.cubicTo(c1x, c1y, c2x, c2y, p2.x, p2.y)
            }
        }
        val fillPath = Path()
        fillPath.addPath(linePath)
        fillPath.lineTo(pts.last().x, h)
        fillPath.lineTo(pts.first().x, h)
        fillPath.close()
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Emerald.copy(alpha = 0.35f), Emerald.copy(alpha = 0.0f)),
                startY = 0f,
                endY = h
            )
        )
        drawPath(
            path = linePath,
            brush = Brush.horizontalGradient(listOf(EmeraldStrong, Emerald)),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )
        val peak = pts.getOrNull(peakIndex)
        if (peak != null) {
            drawLine(
                color = Emerald.copy(alpha = 0.6f),
                start = Offset(peak.x, topPad),
                end = Offset(peak.x, h - bottomPad),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
            drawCircle(color = Color(0xFF0E141C), radius = 11f, center = peak)
            drawCircle(color = Emerald, radius = 11f, center = peak, style = Stroke(width = 5f))
            drawCircle(color = Emerald, radius = 5f, center = peak)
        }
        drawLine(
            color = Color.White.copy(alpha = 0.12f),
            start = Offset(0f, h - 2f),
            end = Offset(w, h - 2f),
            strokeWidth = 2f
        )
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
