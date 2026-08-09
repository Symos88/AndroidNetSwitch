package com.symos.netswitch.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.symos.netswitch.ui.theme.Card
import com.symos.netswitch.ui.theme.CardHigh
import com.symos.netswitch.ui.theme.Line
import com.symos.netswitch.ui.theme.Orange
import com.symos.netswitch.ui.theme.Pink
import com.symos.netswitch.ui.theme.Teal
import com.symos.netswitch.ui.theme.TextDim
import com.symos.netswitch.ui.theme.TextMain

@Composable
fun ModernStatusChip(active: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (active) Teal.copy(alpha = .12f) else CardHigh,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Teal.copy(alpha = .28f) else Line)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            val transition = rememberInfiniteTransition(label = "status")
            val pulse by transition.animateFloat(
                1f, .35f,
                infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "pulse"
            )
            Box(Modifier.size(7.dp).alpha(if (active) pulse else 1f).background(if (active) Teal else Orange, CircleShape))
            Text(if (active) "MONITORING ACTIVE" else "MONITORING OFF", style = MaterialTheme.typography.labelMedium, color = if (active) Teal else TextDim)
        }
    }
}

@Composable
fun ModernStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    detail: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, Line)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).clip(CircleShape).background(accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(9.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = TextDim)
            }
            Text(value, style = MaterialTheme.typography.headlineSmall, color = TextMain)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = TextDim, maxLines = 2)
            Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(accent.copy(alpha = .65f)))
        }
    }
}

@Composable
fun ModernSectionHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextMain)
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = TextDim) }
    }
}
