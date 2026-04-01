package com.timeforyou.app.ui.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeforyou.app.ui.theme.CardShape2xl
import com.timeforyou.app.ui.theme.Spacing
import com.timeforyou.app.ui.theme.WellnessBackgroundBrushes
import com.timeforyou.app.ui.theme.WellnessPrimary

@Composable
fun CoachScreen(viewModel: CoachViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WellnessBackgroundBrushes.screenGradientVertical),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(top = Spacing.xl, bottom = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = "Coach",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape2xl,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    when {
                        state.aiAdviceLoading && state.insightSummary == null ->
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
                            if (state.insightSummary != null) {
                                Text(
                                    text = state.insightSummary.orEmpty(),
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
                            if (state.aiAdviceError != null) {
                                Column(
                                    modifier = Modifier.padding(top = Spacing.sm),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                                ) {
                                    Text(
                                        text = state.aiAdviceError.orEmpty(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = WellnessPrimary,
                                    )
                                    TextButton(onClick = { viewModel.retryCoachAdvice() }) {
                                        Text("Try again")
                                    }
                                }
                            }
                        }
                    }
                    if (state.aiAdviceDisabledReason != null) {
                        Text(
                            text = state.aiAdviceDisabledReason.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            color = WellnessPrimary,
                        )
                    }
                }
            }

            if (state.tips.isNotEmpty()) {
                Text(
                    text = "Motivation & ideas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = Spacing.sm),
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape2xl,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        state.tips.forEachIndexed { index, tip ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Text(
                                    text = tip.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = tip.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}
