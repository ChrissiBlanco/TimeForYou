package com.timeforyou.app.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeforyou.app.ui.theme.CardShape2xl
import com.timeforyou.app.ui.theme.Spacing
import com.timeforyou.app.ui.theme.WellnessBackgroundBrushes
import com.timeforyou.app.ui.theme.WellnessGlow
import com.timeforyou.app.ui.theme.WellnessPrimary
import java.time.ZoneId

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val glow by animateFloatAsState(
        targetValue = if (state.streak > 0) 1f else 0.4f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "streakGlow",
    )
    val todayTarget = (state.todayLogCount / 4f).coerceIn(0f, 1f)
    val todayProgress by animateFloatAsState(
        targetValue = todayTarget,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "todayProgress",
    )
    val zoneId = remember { ZoneId.systemDefault() }
    var showLogMomentDialog by remember { mutableStateOf(false) }
    var logDialogSession by remember { mutableIntStateOf(0) }

    if (showLogMomentDialog) {
        key(logDialogSession) {
            LogMomentDialog(
                zoneId = zoneId,
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        if (state.streak > 0) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        WellnessGlow.copy(alpha = 0.28f * glow),
                                        Color.Transparent,
                                    ),
                                    center = Offset(size.width * 0.88f, size.height * 0.18f),
                                    radius = size.maxDimension * 0.95f,
                                ),
                            )
                        }
                    },
                shape = CardShape2xl,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = (2f + 6f * glow).dp),
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Text(
                        text = "Your streak",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${state.streak} day${if (state.streak == 1) "" else "s"} in a row",
                        style = MaterialTheme.typography.headlineLarge,
                        color = WellnessPrimary,
                    )
                    Text(
                        text = "Keep the chain caring, not perfect.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape2xl,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    Text(
                        text = "Today’s rhythm",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "${state.todayLogCount} moments logged",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { todayProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = WellnessPrimary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        strokeCap = StrokeCap.Round,
                    )
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
            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}
