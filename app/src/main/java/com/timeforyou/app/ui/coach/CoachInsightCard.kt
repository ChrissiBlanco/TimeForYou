package com.timeforyou.app.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timeforyou.app.ui.theme.CardShape2xl
import com.timeforyou.app.ui.theme.Spacing
import com.timeforyou.app.ui.theme.WellnessPrimary

@Composable
internal fun CoachInsightCard(
    insightSummary: String?,
    aiAdviceLoading: Boolean,
    aiAdviceError: String?,
    aiAdviceDisabledReason: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape2xl,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            when {
                aiAdviceLoading && insightSummary == null ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.md),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(36.dp),
                            strokeWidth = 3.dp,
                            color = WellnessPrimary,
                        )
                    }
                else -> {
                    if (insightSummary != null) {
                        Text(
                            text = insightSummary,
                            style = MaterialTheme.typography.titleMedium,
                            color = WellnessPrimary,
                        )
                    } else {
                        Text(
                            text = "Insight will appear in a moment.",
                            style = MaterialTheme.typography.titleMedium,
                            color = WellnessPrimary,
                        )
                    }
                    if (aiAdviceError != null) {
                        Column(
                            modifier = Modifier.padding(top = Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        ) {
                            Text(
                                text = aiAdviceError,
                                style = MaterialTheme.typography.titleMedium,
                                color = WellnessPrimary,
                            )
                            TextButton(onClick = onRetry) {
                                Text("Try again")
                            }
                        }
                    }
                }
            }
            if (aiAdviceDisabledReason != null) {
                Text(
                    text = aiAdviceDisabledReason,
                    style = MaterialTheme.typography.titleMedium,
                    color = WellnessPrimary,
                )
            }
        }
    }
}
