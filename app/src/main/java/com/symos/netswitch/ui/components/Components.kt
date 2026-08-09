package com.symos.netswitch.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Stroke
import androidx.compose.ui.unit.dp
import com.symos.netswitch.ui.theme.Card
import com.symos.netswitch.ui.theme.CardHigh
import com.symos.netswitch.ui.theme.Line
import com.symos.netswitch.ui.theme.Orange
import com.symos.netswitch.ui.theme.Teal
import com.symos.netswitch.ui.theme.TextDim
import com.symos.netswitch.ui.theme.TextMain
import kotlin.math.sin

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = TextDim) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
    )
}

@Composable
fun PulseIndicator(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            tween(1100, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val color = if (active) Teal else Orange
    Box(
        Modifier
            .size(8.dp)
            .alpha(if (active) alpha else 1f)
            .background(color, CircleShape)
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
        shape = RoundedCornerShape(20.dp),
        color = Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, Line)
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (label) {
                    "HOME" -> Icons.Rounded.Home
                    "DISTANCE" -> Icons.Rounded.LocationOn
                    else -> if (value == "ARMED") Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked
                }
                Box(
                    Modifier
                        .size(32.dp)
                        .background(accent.copy(alpha = .11f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(17.dp))
                }
                Spacer(Modifier.width(8.dp))
                SectionLabel(label)
            }
            Spacer(Modifier.height(11.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = TextMain,
                maxLines = 1
            )
            Spacer(Modifier.height(3.dp))
            Text(
                caption,
                style = MaterialTheme.typography.bodySmall,
                color = TextDim,
                maxLines = 2
            )
            Spacer(Modifier.height(11.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(accent.copy(alpha = .7f), RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun WaveLine(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(18.dp)) {
        val path = Path()
        val mid = size.height / 2
        path.moveTo(0f, mid)
        var x = 0f
        while (x <= size.width) {
            val y = mid + (sin(x / 22.0) * 2.4 + sin(x / 57.0) * 1.7).toFloat()
            path.lineTo(x, y)
            x += 3f
        }
        drawPath(path, Teal.copy(alpha = 0.48f), style = Stroke(width = 2f))
    }
}

@Composable
fun AppTopBar() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(9.dp)
                        .background(Teal, CircleShape)
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    "NETSWITCH",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextMain
                )
            }
            Text(
                "LOCATION AUTOMATION",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                modifier = Modifier.padding(start = 18.dp, top = 2.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(50),
            color = Teal.copy(alpha = .09f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Teal.copy(alpha = .2f))
        ) {
            Row(
                Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PulseIndicator(true)
                Spacer(Modifier.width(7.dp))
                Text(
                    "GEOFENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Teal
                )
            }
        }
    }
}

@Composable
fun RowScope.WeightSpacer() = Spacer(Modifier.weight(1f))
