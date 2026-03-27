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
            }.onFailure { error ->
                loadError = context.getString(R.string.failed_to_load_journals)
            }
            isLoading = false
        }
    }

    private fun formatTimestamp(epochSeconds: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date(epochSeconds * 1000L))
    }
}

