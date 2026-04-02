package com.timeforyou.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timeforyou.app.ui.theme.CardShape2xl
import com.timeforyou.app.ui.theme.Spacing
import com.timeforyou.app.ui.theme.WellnessPrimary

@Composable
fun HomeStreakCard(state: HomeUiState, modifier: Modifier = Modifier) {
    val atRisk = state.streakAtRisk
    val hasStreak = state.streak > 0
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape2xl,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = if (atRisk) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.error)
        } else {
            BorderStroke(1.dp, WellnessPrimary)
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (atRisk) 4.dp else 1.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            if (atRisk) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = "Streak at risk",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.LocalFireDepartment,
                    contentDescription =
                        if (hasStreak) {
                            "${state.streak}-day logging streak"
                        } else {
                            "Logging streak: not started"
                        },
                    modifier = Modifier.size(40.dp),
                    tint = when {
                        hasStreak -> WellnessPrimary
                        else -> MaterialTheme.colorScheme.outline
                    },
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                when {
                    atRisk -> {
                        Text(
                            text = "Streak at risk",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = if (state.streak == 1) {
                                "You haven’t logged today—add a moment to keep your 1-day streak alive."
                            } else {
                                "You haven’t logged today—one small moment keeps your " +
                                    "${state.streak}-day streak from resetting."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    hasStreak -> {
                        Text(
                            text = when (state.streak) {
                                1 -> "1-day streak"
                                else -> "${state.streak}-day streak"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = WellnessPrimary,
                        )
                        Text(
                            text = "Logged today — your streak is safe for now.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> {
                        Text(
                            text = "Streak",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Log a moment on a new calendar day to begin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
