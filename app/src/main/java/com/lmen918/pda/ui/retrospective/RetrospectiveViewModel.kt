package com.lmen918.pda.ui.retrospective

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RetrospectiveViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    enum class Phase { INTRO, POSITIVE, MEDIAN, NEGATIVE, JOURNAL, COMPLETE }

    var phase by mutableStateOf(Phase.INTRO)
        private set
    var timeLeftSeconds by mutableIntStateOf(sessionDurationSeconds)
        private set

    val positiveEntries = mutableStateListOf<String>()
    val medianEntries = mutableStateListOf<String>()
    val negativeEntries = mutableStateListOf<String>()
    var journalText by mutableStateOf("")
    var saveError by mutableStateOf<String?>(null)

    private var timerJob: Job? = null

    companion object {
        const val sessionDurationSeconds = 300
    }

    fun startPhase(newPhase: Phase) {
        phase = newPhase
        if (newPhase == Phase.POSITIVE || newPhase == Phase.MEDIAN || newPhase == Phase.NEGATIVE) {
            timeLeftSeconds = sessionDurationSeconds
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timeLeftSeconds > 0) {
                delay(1000L)
                timeLeftSeconds--
            }
            nextPhase()
        }
    }

    fun addEntry(text: String) {
        if (text.isBlank()) return
        when (phase) {
            Phase.POSITIVE -> positiveEntries.add(text.trim())
            Phase.MEDIAN -> medianEntries.add(text.trim())
            Phase.NEGATIVE -> negativeEntries.add(text.trim())
            else -> {}
        }
    }

    fun removeEntry(text: String) {
        when (phase) {
            Phase.POSITIVE -> positiveEntries.remove(text)
            Phase.MEDIAN -> medianEntries.remove(text)
            Phase.NEGATIVE -> negativeEntries.remove(text)
            else -> {}
        }
    }

    fun nextPhase() {
        timerJob?.cancel()
        phase = when (phase) {
            Phase.INTRO -> Phase.POSITIVE
            Phase.POSITIVE -> Phase.MEDIAN
            Phase.MEDIAN -> Phase.NEGATIVE
            Phase.NEGATIVE -> Phase.JOURNAL
            Phase.JOURNAL -> Phase.COMPLETE
            Phase.COMPLETE -> Phase.COMPLETE
        }
        if (phase == Phase.POSITIVE || phase == Phase.MEDIAN || phase == Phase.NEGATIVE) {
            timeLeftSeconds = sessionDurationSeconds
            startTimer()
        }
    }

    fun saveJournal(uri: Uri) {
        viewModelScope.launch {
            try {
                val markdown = buildMarkdown()
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(markdown.toByteArray())
                }
                phase = Phase.COMPLETE
            } catch (e: Exception) {
                saveError = e.message ?: "Failed to save journal"
            }
        }
    }

    fun buildMarkdown(): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val sb = StringBuilder()
        sb.appendLine("# Retrospective - $dateStr")
        sb.appendLine()
        sb.appendLine("## ✅ Positive")
        positiveEntries.forEach { sb.appendLine("- $it") }
        sb.appendLine()
        sb.appendLine("## ➡️ Neutral")
        medianEntries.forEach { sb.appendLine("- $it") }
        sb.appendLine()
        sb.appendLine("## ❌ Negative")
        negativeEntries.forEach { sb.appendLine("- $it") }
        sb.appendLine()
        sb.appendLine("## Journal")
        sb.appendLine(journalText)
        return sb.toString()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
