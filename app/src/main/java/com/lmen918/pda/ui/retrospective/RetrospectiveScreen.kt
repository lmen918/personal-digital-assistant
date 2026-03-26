package com.lmen918.pda.ui.retrospective

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lmen918.pda.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetrospectiveScreen(
    viewModel: RetrospectiveViewModel = hiltViewModel()
) {
    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri ->
        uri?.let { viewModel.saveJournal(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.retrospective)) })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (viewModel.phase) {
                RetrospectiveViewModel.Phase.INTRO -> IntroPhase(
                    onStart = { viewModel.startPhase(RetrospectiveViewModel.Phase.POSITIVE) }
                )
                RetrospectiveViewModel.Phase.POSITIVE -> SessionPhase(
                    title = stringResource(R.string.positive_session),
                    timeLeft = viewModel.timeLeftSeconds,
                    entries = viewModel.positiveEntries,
                    onAddEntry = { viewModel.addEntry(it) },
                    onRemoveEntry = { viewModel.removeEntry(it) },
                    onNext = { viewModel.nextPhase() }
                )
                RetrospectiveViewModel.Phase.MEDIAN -> SessionPhase(
                    title = stringResource(R.string.median_session),
                    timeLeft = viewModel.timeLeftSeconds,
                    entries = viewModel.medianEntries,
                    onAddEntry = { viewModel.addEntry(it) },
                    onRemoveEntry = { viewModel.removeEntry(it) },
                    onNext = { viewModel.nextPhase() }
                )
                RetrospectiveViewModel.Phase.NEGATIVE -> SessionPhase(
                    title = stringResource(R.string.negative_session),
                    timeLeft = viewModel.timeLeftSeconds,
                    entries = viewModel.negativeEntries,
                    onAddEntry = { viewModel.addEntry(it) },
                    onRemoveEntry = { viewModel.removeEntry(it) },
                    onNext = { viewModel.nextPhase() }
                )
                RetrospectiveViewModel.Phase.JOURNAL -> JournalPhase(
                    positiveEntries = viewModel.positiveEntries,
                    medianEntries = viewModel.medianEntries,
                    negativeEntries = viewModel.negativeEntries,
                    journalText = viewModel.journalText,
                    onJournalTextChange = { viewModel.journalText = it },
                    onSave = {
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault())
                            .format(java.util.Date())
                        createDocumentLauncher.launch("retrospective_$timestamp.md")
                    }
                )
                RetrospectiveViewModel.Phase.COMPLETE -> CompletePhase(
                    onReset = { viewModel.startPhase(RetrospectiveViewModel.Phase.INTRO) }
                )
            }
        }
    }

    viewModel.saveError?.let { err ->
        AlertDialog(
            onDismissRequest = { viewModel.saveError = null },
            title = { Text("Error") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = { viewModel.saveError = null }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun IntroPhase(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.retro_intro_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.retro_intro_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.start))
        }
    }
}

@Composable
private fun SessionPhase(
    title: String,
    timeLeft: Int,
    entries: List<String>,
    onAddEntry: (String) -> Unit,
    onRemoveEntry: (String) -> Unit,
    onNext: () -> Unit
) {
    var entryText by remember { mutableStateOf("") }
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.titleMedium,
                color = if (timeLeft <= 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { timeLeft.toFloat() / RetrospectiveViewModel.sessionDurationSeconds },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = entryText,
            onValueChange = { entryText = it },
            placeholder = { Text(stringResource(R.string.add_entry_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .onKeyEvent { event ->
                    if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                        if (entryText.isNotBlank()) {
                            onAddEntry(entryText)
                            entryText = ""
                        }
                        true
                    } else false
                },
            trailingIcon = {
                if (entryText.isNotBlank()) {
                    IconButton(onClick = {
                        onAddEntry(entryText)
                        entryText = ""
                    }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(entries) { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("• ", color = MaterialTheme.colorScheme.primary)
                    Text(entry, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onRemoveEntry(entry) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.next))
        }
    }
}

@Composable
private fun JournalPhase(
    positiveEntries: List<String>,
    medianEntries: List<String>,
    negativeEntries: List<String>,
    journalText: String,
    onJournalTextChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.journal_session),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        EntrySummarySection(title = stringResource(R.string.positive_session), entries = positiveEntries)
        EntrySummarySection(title = stringResource(R.string.median_session), entries = medianEntries)
        EntrySummarySection(title = stringResource(R.string.negative_session), entries = negativeEntries)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Journal Entry", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = journalText,
            onValueChange = onJournalTextChange,
            placeholder = { Text(stringResource(R.string.journal_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            minLines = 8
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.save_journal))
        }
    }
}

@Composable
private fun EntrySummarySection(title: String, entries: List<String>) {
    if (entries.isNotEmpty()) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        entries.forEach { entry ->
            Text("• $entry", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CompletePhase(onReset: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.retro_complete),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.retro_saved),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
            Text("Start New Retrospective")
        }
    }
}
