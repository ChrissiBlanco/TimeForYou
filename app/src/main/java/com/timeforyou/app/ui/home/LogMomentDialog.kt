package com.timeforyou.app.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
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

private val defaultLogMomentSuggestions = listOf(
    "Short walk",
    "Breathing pause",
    "Stretch break",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogMomentDialog(
    zoneId: ZoneId,
    suggestions: List<String> = defaultLogMomentSuggestions,
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
    var pickedTime by remember { mutableStateOf(LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        val baseTime = pickedTime
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

    val resolvedTimestamp = LocalDate.now(zoneId)
        .atTime(pickedTime)
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()

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
                    label = { Text("Activity") },
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
                    suggestions.take(3).forEach { suggestion ->
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
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = pickedTime.format(timeLabelFormatter),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "Change time",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    )
}
