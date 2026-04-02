package com.timeforyou.app.ui.home

import android.text.format.DateFormat
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeforyou.app.ui.theme.Spacing
import com.timeforyou.app.ui.theme.WellnessBackgroundBrushes
import com.timeforyou.app.ui.theme.WellnessPrimary
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

            HomeStreakCard(state = state)

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

            HomeTodaysMomentsSection(
                moments = state.todaysMoments,
                zoneId = zoneId,
                timeFormatter = timeFormatter,
            )

            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}
