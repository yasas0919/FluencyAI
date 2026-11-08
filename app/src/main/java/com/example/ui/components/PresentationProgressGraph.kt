package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PresentationEntity
import com.example.ui.theme.CardSurfaceBorder
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.VioletNeon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun PresentationProgressGraph(
    presentations: List<PresentationEntity>,
    modifier: Modifier = Modifier
) {
    // If empty, provide sample benchmark entries so user can preview graph capabilities
    val isSample = presentations.isEmpty()
    val displayData = if (isSample) {
        listOf(
            PresentationEntity(1, "Introductory Pitch", "", "", 7.2f, 74, 76, 70, 115, 6, "", "Good opening", "", "", "", 60, System.currentTimeMillis() - 86400000L * 4),
            PresentationEntity(2, "Climate Tech Solutions", "", "", 7.8f, 79, 81, 78, 128, 4, "", "Clear pacing", "", "", "", 75, System.currentTimeMillis() - 86400000L * 3),
            PresentationEntity(3, "Engineering Architecture", "", "", 8.5f, 85, 87, 84, 135, 2, "", "Strong transition", "", "", "", 90, System.currentTimeMillis() - 86400000L * 2),
            PresentationEntity(4, "Executive Strategy", "", "", 9.1f, 92, 90, 89, 140, 1, "", "Executive eloquence", "", "", "", 110, System.currentTimeMillis() - 86400000L)
        )
    } else {
        presentations.sortedBy { it.timestamp }
    }

    var selectedIndex by remember { mutableStateOf<Int?>(displayData.lastIndex) }
    val selectedItem = selectedIndex?.let { displayData.getOrNull(it) }

    val scores = displayData.map { it.scoreOutOfTen }
    val avgScore = String.format(Locale.US, "%.1f", scores.average())
    val bestScore = String.format(Locale.US, "%.1f", scores.maxOrNull() ?: 0f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("presentation_progress_graph_card"),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardSurfaceBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRESENTATION PROGRESS GRAPH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Score Trend (out of 10.0)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Avg: $avgScore/10",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate200,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = EmeraldGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Best: $bestScore/10",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (isSample) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Showing benchmark trajectory. Complete presentations to record your personal trend!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Canvas Line Chart
            var pointOffsets by remember { mutableStateOf<List<Offset>>(emptyList()) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .pointerInput(displayData) {
                            detectTapGestures { tapOffset ->
                                // Find closest point
                                val closest = pointOffsets.indices.minByOrNull { i ->
                                    abs(pointOffsets[i].x - tapOffset.x)
                                }
                                if (closest != null) {
                                    selectedIndex = closest
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingLeft = 32.dp.toPx()
                    val paddingRight = 24.dp.toPx()
                    val paddingTop = 20.dp.toPx()
                    val paddingBottom = 28.dp.toPx()

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    // Draw subtle grid horizontal lines (4, 6, 8, 10)
                    val gridLines = listOf(4.0f, 6.0f, 8.0f, 10.0f)
                    gridLines.forEach { scoreVal ->
                        val y = paddingTop + chartHeight * (1f - (scoreVal / 10f))
                        drawLine(
                            color = Slate800,
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Compute points
                    val minScore = 4.0f
                    val maxScore = 10.0f
                    val count = displayData.size
                    val stepX = if (count > 1) chartWidth / (count - 1) else chartWidth / 2f

                    val points = displayData.mapIndexed { index, item ->
                        val x = if (count > 1) paddingLeft + index * stepX else width / 2f
                        val normalized = ((item.scoreOutOfTen - minScore) / (maxScore - minScore)).coerceIn(0f, 1f)
                        val y = paddingTop + chartHeight * (1f - normalized)
                        Offset(x, y)
                    }
                    pointOffsets = points

                    // Draw Gradient Fill under line
                    if (points.isNotEmpty()) {
                        val path = Path().apply {
                            moveTo(points.first().x, height - paddingBottom)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, height - paddingBottom)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(CyanNeon.copy(alpha = 0.35f), Color.Transparent),
                                startY = paddingTop,
                                endY = height - paddingBottom
                            )
                        )

                        // Draw Curve Stroke
                        val strokePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val cur = points[i]
                                val cx = (prev.x + cur.x) / 2f
                                cubicTo(cx, prev.y, cx, cur.y, cur.x, cur.y)
                            }
                        }
                        drawPath(
                            path = strokePath,
                            brush = Brush.horizontalGradient(listOf(CyanNeon, VioletNeon)),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Draw points
                    points.forEachIndexed { i, pt ->
                        val isSel = i == selectedIndex
                        drawCircle(
                            color = if (isSel) CyanNeon else VioletNeon,
                            radius = if (isSel) 7.dp.toPx() else 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Slate900,
                            radius = if (isSel) 3.5.dp.toPx() else 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            // Interactive Tooltip on Selected Point
            selectedItem?.let { item ->
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.topic,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(item.timestamp))
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Pronunciation: ${item.pronunciationScore}% • Pacing: ${item.pacingScore}% • WPM: ${item.wordsPerMinute}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate200,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Surface(
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", item.scoreOutOfTen)}/10",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
