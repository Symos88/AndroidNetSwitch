package com.symdev.netswitch.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.symdev.netswitch.ui.theme.Card
import com.symdev.netswitch.ui.theme.CardHigh
import com.symdev.netswitch.ui.theme.Line
import com.symdev.netswitch.ui.theme.Orange
import com.symdev.netswitch.ui.theme.Teal
import com.symdev.netswitch.ui.theme.TextDim
import com.symdev.netswitch.ui.theme.TextMain
import kotlin.math.sin

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = TextDim) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = color, modifier = modifier)
}

@Composable
fun PulseIndicator(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha"
    )
    val color = if (active) Teal else Orange
    Box(
        Modifier
            .size(10.dp)
            .background(if (active) color.copy(alpha = alpha) else color, CircleShape)
    )
}

@Composable
fun StatCard(
    label: String,
    value: String,
    caption: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Card,
        border = BorderStroke(1.dp, Line)
    ) {
        Column(Modifier.padding(14.dp)) {
            SectionLabel(label)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = accent)
            Spacer(Modifier.height(4.dp))
            Text(caption, style = MaterialTheme.typography.bodySmall, color = TextDim, maxLines = 2)
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(accent, RoundedCornerShape(2.dp))
            )
        }
    }
}

/** Decorative telemetry line, echoing the dashboard header. */
@Composable
fun WaveLine(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(26.dp)) {
        val path = Path()
        val mid = size.height / 2
        path.moveTo(0f, mid)
        var x = 0f
        while (x <= size.width) {
            val y = mid + (sin(x / 18.0) * 3.2 + sin(x / 47.0) * 2.1).toFloat()
            path.lineTo(x, y)
            x += 3f
        }
        drawPath(path, Teal.copy(alpha = 0.8f), style = Stroke(width = 2f))
    }
}

@Composable
fun AppTopBar() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("NETSWITCH", style = MaterialTheme.typography.titleLarge, color = TextMain)
            Box(
                Modifier
                    .padding(top = 3.dp)
                    .width(56.dp)
                    .height(3.dp)
                    .background(Teal, RoundedCornerShape(2.dp))
            )
        }
        Spacer(Modifier.weight(1f))
        Surface(shape = RoundedCornerShape(6.dp), color = CardHigh) {
            Text(
                "GEOFENCE",
                style = MaterialTheme.typography.labelSmall,
                color = Teal,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun RowScope.WeightSpacer() = Spacer(Modifier.weight(1f))
