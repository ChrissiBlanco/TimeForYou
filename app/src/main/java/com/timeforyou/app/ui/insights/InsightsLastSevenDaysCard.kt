package com.timeforyou.app.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timeforyou.app.domain.model.DayAggregate
import com.timeforyou.app.ui.theme.CardShape2xl
import com.timeforyou.app.ui.theme.Spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun InsightsLastSevenDaysCard(
    lastSevenDays: List<DayAggregate>,
    modifier: Modifier = Modifier,
) {
    val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    val maxCount = (lastSevenDays.maxOfOrNull { it.logCount } ?: 0).coerceAtLeast(1)
    val zone = ZoneId.systemDefault()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape2xl,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = "Last 7 days",
                style = MaterialTheme.typography.titleLarge,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.Bottom,
            ) {
                lastSevenDays.forEach { day ->
                    val dayLabel = Instant.ofEpochMilli(day.dayStartEpochMillis)
                        .atZone(zone)
                        .toLocalDate()
                        .format(dayFormatter)
                    InsightsWeekDayBar(
                        day = day,
                        maxCount = maxCount,
                        dayLabel = dayLabel,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
