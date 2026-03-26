package com.lmen918.pda.ui.events

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lmen918.pda.R
import com.lmen918.pda.domain.model.Event
import com.lmen918.pda.domain.model.Tag
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val allTags by viewModel.tags.collectAsStateWithLifecycle()

    var existingEvent by remember { mutableStateOf<Event?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(System.currentTimeMillis() + 3600_000L) }
    var selectedTags by remember { mutableStateOf<List<Tag>>(emptyList()) }
    var notifyInApp by remember { mutableStateOf(false) }
    var notifyMinutesBefore by remember { mutableIntStateOf(15) }
    var syncToCalendar by remember { mutableStateOf(false) }
    var isAllDay by remember { mutableStateOf(false) }

    var hasCalendarPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        )
    }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasCalendarPermission = perms[Manifest.permission.WRITE_CALENDAR] == true
    }

    LaunchedEffect(eventId) {
        if (eventId != null) {
            viewModel.getEventById(eventId)?.let { event ->
                existingEvent = event
                title = event.title
                description = event.description
                startTime = event.startTime
                endTime = event.endTime
                selectedTags = event.tags
                notifyInApp = event.notifyInApp
                notifyMinutesBefore = event.notifyMinutesBefore
                isAllDay = event.isAllDay
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventId == null) stringResource(R.string.add_event) else stringResource(R.string.edit_event)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (existingEvent != null) {
                        IconButton(onClick = {
                            viewModel.deleteEvent(existingEvent!!)
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.start_time))
                    TextButton(onClick = { showStartDatePicker = true }) {
                        Text(dateFormat.format(Date(startTime)))
                    }
                }
            }

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.end_time))
                    TextButton(onClick = { showEndDatePicker = true }) {
                        Text(dateFormat.format(Date(endTime)))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("All Day")
                Switch(checked = isAllDay, onCheckedChange = { isAllDay = it })
            }

            if (allTags.isNotEmpty()) {
                Text(text = "Tags", style = MaterialTheme.typography.labelLarge)
                FlowTagSelector(allTags = allTags, selectedTags = selectedTags, onTagToggle = { tag ->
                    selectedTags = if (selectedTags.any { it.id == tag.id }) {
                        selectedTags.filter { it.id != tag.id }
                    } else {
                        selectedTags + tag
                    }
                })
            }

            Text(stringResource(R.string.notification_options), style = MaterialTheme.typography.labelLarge)
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !notifyInApp, onClick = { notifyInApp = false })
                    Text(stringResource(R.string.no_notification))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = notifyInApp, onClick = { notifyInApp = true })
                    Text(stringResource(R.string.in_app_notification))
                }
                if (notifyInApp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Slider(
                            value = notifyMinutesBefore.toFloat(),
                            onValueChange = { notifyMinutesBefore = it.toInt() },
                            valueRange = 5f..60f,
                            steps = 10,
                            modifier = Modifier.weight(1f)
                        )
                        Text("$notifyMinutesBefore ${stringResource(R.string.minutes_before)}")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.sync_to_calendar))
                Switch(
                    checked = syncToCalendar,
                    onCheckedChange = { checked ->
                        if (checked && !hasCalendarPermission) {
                            calendarPermissionLauncher.launch(
                                arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                            )
                        } else {
                            syncToCalendar = checked
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val event = Event(
                            id = existingEvent?.id ?: 0L,
                            title = title.trim(),
                            description = description.trim(),
                            startTime = startTime,
                            endTime = endTime,
                            tags = selectedTags,
                            calendarEventId = existingEvent?.calendarEventId,
                            notifyInApp = notifyInApp,
                            notifyMinutesBefore = notifyMinutesBefore,
                            isAllDay = isAllDay
                        )
                        viewModel.saveEvent(event, syncToCalendar)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }

    if (showStartDatePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = startTime }
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            onDateSelected = { year, month, day ->
                val c = Calendar.getInstance().apply {
                    timeInMillis = startTime
                    set(year, month, day)
                }
                startTime = c.timeInMillis
                showStartDatePicker = false
                showStartTimePicker = true
            },
            initialYear = cal.get(Calendar.YEAR),
            initialMonth = cal.get(Calendar.MONTH),
            initialDay = cal.get(Calendar.DAY_OF_MONTH)
        )
    }
    if (showEndDatePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = endTime }
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            onDateSelected = { year, month, day ->
                val c = Calendar.getInstance().apply {
                    timeInMillis = endTime
                    set(year, month, day)
                }
                endTime = c.timeInMillis
                showEndDatePicker = false
                showEndTimePicker = true
            },
            initialYear = cal.get(Calendar.YEAR),
            initialMonth = cal.get(Calendar.MONTH),
            initialDay = cal.get(Calendar.DAY_OF_MONTH)
        )
    }
    if (showStartTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = startTime }
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            onTimeSelected = { hour, minute ->
                val c = Calendar.getInstance().apply {
                    timeInMillis = startTime
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                startTime = c.timeInMillis
                showStartTimePicker = false
            },
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
    }
    if (showEndTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = endTime }
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            onTimeSelected = { hour, minute ->
                val c = Calendar.getInstance().apply {
                    timeInMillis = endTime
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                endTime = c.timeInMillis
                showEndTimePicker = false
            },
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
    }
}

@Composable
fun FlowTagSelector(
    allTags: List<Tag>,
    selectedTags: List<Tag>,
    onTagToggle: (Tag) -> Unit
) {
    val rows = mutableListOf<List<Tag>>()
    var currentRow = mutableListOf<Tag>()
    allTags.forEach { tag ->
        currentRow.add(tag)
        if (currentRow.size == 4) {
            rows.add(currentRow.toList())
            currentRow = mutableListOf()
        }
    }
    if (currentRow.isNotEmpty()) rows.add(currentRow.toList())

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { tag ->
                    val isSelected = selectedTags.any { it.id == tag.id }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTagToggle(tag) },
                        label = { Text(tag.name) },
                        leadingIcon = {
                            val color = com.lmen918.pda.ui.components.parseColor(tag.colorHex)
                            Box(modifier = Modifier
                                .size(8.dp)
                                .background(color, shape = MaterialTheme.shapes.small))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Int, Int, Int) -> Unit,
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = Calendar.getInstance().apply {
            set(initialYear, initialMonth, initialDay, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    )
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    val cal = Calendar.getInstance().apply { timeInMillis = millis }
                    onDateSelected(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                }
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Time") },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onTimeSelected(state.hour, state.minute) }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        }
    )
}
