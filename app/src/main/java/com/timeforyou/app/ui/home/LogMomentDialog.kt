package com.timeforyou.app.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.timeforyou.app.ui.theme.Spacing
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val logMomentActivitySuggestions = listOf(
    "Short walk",
    "Breathing pause",
    "Stretch break",
    "Tea or water",
    "Journal note",
    "Phone someone kind",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogMomentDialog(
    zoneId: ZoneId,
    onDismiss: () -> Unit,
    onSave: (activityNote: String?, timestampEpochMillis: Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)
    val timeLabelFormatter = remember(is24Hour) {
        DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a")
    }

    var activityText by remember { mutableStateOf("") }
    var pickedTime by remember { mutableStateOf<LocalTime?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        val baseTime = pickedTime ?: LocalTime.now()
        val timePickerState = rememberTimePickerState(
            initialHour = baseTime.hour,
            initialMinute = baseTime.minute,
            is24Hour = is24Hour,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = { TimePicker(state = timePickerState) },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        )
    }

    val resolvedTimestamp = pickedTime?.let { t ->
        LocalDate.now(zoneId)
            .atTime(t)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val note = activityText.trim().takeIf { it.isNotEmpty() }
                    onSave(note, resolvedTimestamp)
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Log a moment") },
        text = {
            Column(
                modifier = modifier
                    .widthIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                OutlinedTextField(
                    value = activityText,
                    onValueChange = { activityText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Activity (optional)") },
                    placeholder = { Text("What did you do?") },
                    singleLine = false,
                    minLines = 1,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    logMomentActivitySuggestions.forEach { suggestion ->
                        FilterChip(
                            selected = activityText == suggestion,
                            onClick = { activityText = suggestion },
                            label = {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                )
                            },
                            modifier = Modifier.height(26.dp),
                            shape = RoundedCornerShape(8.dp),
                        )
                    }
                }
                Text(
                    text = "Time (optional)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                when (val t = pickedTime) {
                    null -> {
                        Text(
                            text = "If you skip this, we’ll use the current time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = { showTimePicker = true }) {
                            Text("Choose time")
                        }
                    }
                    else -> {
                        Text(
                            text = t.format(timeLabelFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        TextButton(onClick = { pickedTime = null }) {
                            Text("Clear time")
                        }
                        TextButton(onClick = { showTimePicker = true }) {
                            Text("Change")
                        }
                    }
                }
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    )
}
