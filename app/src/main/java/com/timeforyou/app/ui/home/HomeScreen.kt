package com.timeforyou.app.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeforyou.app.domain.model.BehaviorLog
import com.timeforyou.app.ui.theme.CardShape2xl
import com.timeforyou.app.ui.theme.Spacing
import com.timeforyou.app.ui.theme.WellnessBackgroundBrushes
import com.timeforyou.app.ui.theme.WellnessPrimary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(State.RESUMED)) {
            viewModel.onResume()
        }
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val zoneId = remember { ZoneId.systemDefault() }
    val context = LocalContext.current
    val timeFormatter = remember(context) {
        DateTimeFormatter.ofPattern(
            if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a",
            Locale.getDefault(),
        )
    }
    var showLogMomentDialog by remember { mutableStateOf(false) }
    var logDialogSession by remember { mutableIntStateOf(0) }

    if (showLogMomentDialog) {
        key(logDialogSession) {
            LogMomentDialog(
                zoneId = zoneId,
                suggestions = state.momentSuggestions,
                onDismiss = { showLogMomentDialog = false },
                onSave = { note, timestamp ->
                    viewModel.logMoment(note, timestamp)
                    showLogMomentDialog = false
                },
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WellnessBackgroundBrushes.screenGradientVertical),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(top = Spacing.xl, bottom = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Text(
                text = state.headline,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = state.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (state.needsTodayLogReminder) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape2xl,
                    colors = CardDefaults.cardColors(
                        containerColor = WellnessPrimary.copy(alpha = 0.14f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint = WellnessPrimary,
                            )
                            Text(
                                text = "Log a moment for today",
                                style = MaterialTheme.typography.titleMedium,
                                color = WellnessPrimary,
                            )
                        }
                        Text(
                            text = "Tap the button when you’re ready—small pauses count.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            FilledTonalButton(
                onClick = {
                    logDialogSession++
                    showLogMomentDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = WellnessPrimary.copy(alpha = 0.22f),
                    contentColor = WellnessPrimary,
                ),
            ) {
                Text(
                    text = "Log a moment",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = Spacing.sm),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalFireDepartment,
                    contentDescription =
                        if (state.streak > 0) {
                            "${state.streak}-day logging streak"
                        } else {
                            "Logging streak: not started"
                        },
                    modifier = Modifier.size(28.dp),
                    tint = if (state.streak > 0) WellnessPrimary else MaterialTheme.colorScheme.outline,
                )
                Column() {
                    Text(
                        text = when {
                            state.streak <= 0 -> "Streak"
                            state.streak == 1 -> "1 day streak"
                            else -> "${state.streak}-day streak"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            if (state.todaysMoments.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    state.todaysMoments.forEach { log ->
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

            Spacer(modifier = Modifier.height(Spacing.md))
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
