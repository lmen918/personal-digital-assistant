package com.lmen918.pda.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lmen918.pda.R
import com.lmen918.pda.reminder.ReminderFrequency
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSaveAndNavigateBack: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.reminder_section_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.reminder_section_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.enable_reminders), style = MaterialTheme.typography.titleSmall)
                        Switch(
                            checked = viewModel.reminderEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateReminderEnabled(enabled)
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                    }

                    Text(stringResource(R.string.reminder_frequency), style = MaterialTheme.typography.titleSmall)
                    FrequencyRow(
                        selected = viewModel.frequency,
                        onSelected = viewModel::updateFrequency
                    )

                    if (viewModel.frequency == ReminderFrequency.WEEKLY) {
                        Text(stringResource(R.string.reminder_day_of_week), style = MaterialTheme.typography.titleSmall)
                        WeeklyDayRow(
                            selectedDay = viewModel.dayOfWeek,
                            onSelected = viewModel::updateDayOfWeek
                        )
                    } else {
                        Text(stringResource(R.string.reminder_day_of_month), style = MaterialTheme.typography.titleSmall)
                        MonthlyDayRow(
                            selectedDay = viewModel.dayOfMonth,
                            onSelected = viewModel::updateDayOfMonth
                        )
                    }

                    Text(stringResource(R.string.reminder_time), style = MaterialTheme.typography.titleSmall)
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> viewModel.setTime(hour, minute) },
                                viewModel.hourOfDay,
                                viewModel.minute,
                                false
                            ).show()
                        }
                    ) {
                        Text(formatTime(viewModel.hourOfDay, viewModel.minute))
                    }
                }
            }

            Button(
                onClick = {
                    if (isSaving) return@Button
                    scope.launch {
                        isSaving = true
                        val message = viewModel.saveReminderSettings()
                        isSaving = false
                        onSaveAndNavigateBack(message)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text(
                    if (isSaving) stringResource(R.string.saving_settings)
                    else stringResource(R.string.save_settings)
                )
            }
        }
    }
}

@Composable
private fun FrequencyRow(
    selected: ReminderFrequency,
    onSelected: (ReminderFrequency) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        FrequencyOption(
            label = stringResource(R.string.weekly),
            selected = selected == ReminderFrequency.WEEKLY,
            onSelected = { onSelected(ReminderFrequency.WEEKLY) }
        )
        FrequencyOption(
            label = stringResource(R.string.monthly),
            selected = selected == ReminderFrequency.MONTHLY,
            onSelected = { onSelected(ReminderFrequency.MONTHLY) }
        )
    }
}

@Composable
private fun FrequencyOption(label: String, selected: Boolean, onSelected: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onSelected)
        Text(label)
    }
}

@Composable
private fun WeeklyDayRow(selectedDay: Int, onSelected: (Int) -> Unit) {
    val dayLabels = remember {
        linkedMapOf(
            Calendar.SUNDAY to DateFormatSymbols(Locale.getDefault()).shortWeekdays[Calendar.SUNDAY],
            Calendar.MONDAY to DateFormatSymbols(Locale.getDefault()).shortWeekdays[Calendar.MONDAY],
            Calendar.TUESDAY to DateFormatSymbols(Locale.getDefault()).shortWeekdays[Calendar.TUESDAY],
            Calendar.WEDNESDAY to DateFormatSymbols(Locale.getDefault()).shortWeekdays[Calendar.WEDNESDAY],
            Calendar.THURSDAY to DateFormatSymbols(Locale.getDefault()).shortWeekdays[Calendar.THURSDAY],
            Calendar.FRIDAY to DateFormatSymbols(Locale.getDefault()).shortWeekdays[Calendar.FRIDAY],
            Calendar.SATURDAY to DateFormatSymbols(Locale.getDefault()).shortWeekdays[Calendar.SATURDAY]
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        dayLabels.forEach { (value, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedDay == value, onClick = { onSelected(value) })
                Text(label)
            }
        }
    }
}

@Composable
private fun MonthlyDayRow(selectedDay: Int, onSelected: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onSelected((selectedDay - 1).coerceAtLeast(1)) }) {
                Text("-")
            }
            Text(text = selectedDay.toString(), style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = { onSelected((selectedDay + 1).coerceAtMost(31)) }) {
                Text("+")
            }
        }
        Text(
            text = stringResource(R.string.monthly_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTime(hourOfDay: Int, minute: Int): String {
    return String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
}


