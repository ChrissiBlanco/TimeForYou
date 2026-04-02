package com.timeforyou.app.ui.insights

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.timeforyou.app.domain.model.DayAggregate
import com.timeforyou.app.ui.theme.Spacing
import com.timeforyou.app.ui.theme.WellnessPrimary

@Composable
internal fun InsightsWeekDayBar(
    day: DayAggregate,
    maxCount: Int,
    dayLabel: String,
    modifier: Modifier = Modifier,
) {
    val fraction = day.logCount.toFloat() / maxCount.toFloat()
    val barAnim by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "bar_${day.dayStartEpochMillis}",
    )
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .fillMaxHeight(barAnim.coerceIn(0.08f, 1f))
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (day.logCount > 0) {
                            WellnessPrimary.copy(alpha = 0.85f)
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        },
                    ),
            )
        }
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = day.logCount.toString(),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
