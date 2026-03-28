package com.lmen918.pda.ui.journalentries

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmen918.pda.R
import com.lmen918.pda.journal.JournalStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.launch

data class JournalEntryUi(
    val name: String,
    val modifiedLabel: String,
    val content: String
)

@HiltViewModel
class JournalEntriesViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val entries = mutableStateListOf<JournalEntryUi>()
    var isLoading by mutableStateOf(true)
        private set
    var loadError by mutableStateOf<String?>(null)
        private set

    // Edit state
    var editingEntry by mutableStateOf<JournalEntryUi?>(null)
        private set
    var editText by mutableStateOf("")
    var isSavingEdit by mutableStateOf(false)
        private set
    var editError by mutableStateOf<String?>(null)

    // Delete state
    var pendingDeleteEntry by mutableStateOf<JournalEntryUi?>(null)
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            loadError = null
            runCatching {
                JournalStorage.listJournalEntries(context)
                    .map { entry ->
                        JournalEntryUi(
                            name = entry.name,
                            modifiedLabel = formatTimestamp(entry.modifiedAtEpochSeconds),
                            content = entry.content
                        )
                    }
            }.onSuccess { loadedEntries ->
                entries.clear()
                entries.addAll(loadedEntries)
            }.onFailure {
                loadError = context.getString(R.string.failed_to_load_journals)
            }
            isLoading = false
        }
    }

    fun startEdit(entry: JournalEntryUi) {
        editingEntry = entry
        editText = entry.content
    }

    fun cancelEdit() {
        editingEntry = null
        editText = ""
        editError = null
    }

    fun saveEdit() {
        val entry = editingEntry ?: return
        viewModelScope.launch {
            isSavingEdit = true
            runCatching {
                JournalStorage.updateJournal(context, entry.name, editText)
            }.onSuccess {
                val index = entries.indexOfFirst { it.name == entry.name }
                if (index >= 0) entries[index] = entries[index].copy(content = editText)
                editingEntry = null
                editText = ""
                editError = null
            }.onFailure { error ->
                editError = error.message ?: context.getString(R.string.edit_save_error)
            }
            isSavingEdit = false
        }
    }

    fun requestDelete(entry: JournalEntryUi) {
        pendingDeleteEntry = entry
    }

    fun cancelDelete() {
        pendingDeleteEntry = null
    }

    fun confirmDelete() {
        val entry = pendingDeleteEntry ?: return
        pendingDeleteEntry = null
        viewModelScope.launch {
            runCatching {
                JournalStorage.deleteJournal(context, entry.name)
            }.onSuccess {
                entries.remove(entry)
            }.onFailure { error ->
                loadError = error.message ?: context.getString(R.string.delete_error)
            }
        }
    }

    private fun formatTimestamp(epochSeconds: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date(epochSeconds * 1000L))
    }
}
