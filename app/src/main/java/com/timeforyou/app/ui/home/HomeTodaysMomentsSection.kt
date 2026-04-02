package com.timeforyou.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.timeforyou.app.domain.model.BehaviorLog
import com.timeforyou.app.ui.theme.CardShape2xl
import com.timeforyou.app.ui.theme.Spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeTodaysMomentsSection(
    moments: List<BehaviorLog>,
    zoneId: ZoneId,
    timeFormatter: DateTimeFormatter,
    modifier: Modifier = Modifier,
) {
    if (moments.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = "Today",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        moments.forEach { log ->
            key(log.id) {
                HomeMomentListItem(
                    log = log,
                    zoneId = zoneId,
                    timeFormatter = timeFormatter,
                )
            }
        }
    }
}

@Composable
private fun HomeMomentListItem(
    log: BehaviorLog,
    zoneId: ZoneId,
    timeFormatter: DateTimeFormatter,
) {
    val timeText = remember(log.timestampEpochMillis, timeFormatter) {
        Instant.ofEpochMilli(log.timestampEpochMillis)
            .atZone(zoneId)
            .toLocalTime()
            .format(timeFormatter)
    }
    val bodyText = log.note?.takeIf { it.isNotBlank() } ?: "Moment"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape2xl,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
