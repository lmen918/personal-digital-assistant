package com.lmen918.pda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lmen918.pda.ui.journalentries.JournalEntriesScreen
import com.lmen918.pda.ui.retrospective.RetrospectiveScreen
import com.lmen918.pda.ui.settings.SettingsScreen
import com.lmen918.pda.ui.theme.PdaTheme
import dagger.hilt.android.AndroidEntryPoint

private enum class AppScreen {
    RETROSPECTIVE,
    SETTINGS,
    JOURNALS
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentScreen by remember { mutableStateOf(AppScreen.RETROSPECTIVE) }
            var settingsSavedMessage by remember { mutableStateOf<String?>(null) }
            PdaTheme {
                when (currentScreen) {
                    AppScreen.RETROSPECTIVE -> RetrospectiveScreen(
                        onOpenSettings = { currentScreen = AppScreen.SETTINGS },
                        onOpenJournals = { currentScreen = AppScreen.JOURNALS },
                        settingsSavedMessage = settingsSavedMessage,
                        onSettingsSavedMessageShown = { settingsSavedMessage = null }
                    )
                    AppScreen.SETTINGS -> SettingsScreen(
                        onNavigateBack = { currentScreen = AppScreen.RETROSPECTIVE },
                        onSaveAndNavigateBack = { message ->
                            settingsSavedMessage = message
                            currentScreen = AppScreen.RETROSPECTIVE
                        }
                    )
                    AppScreen.JOURNALS -> JournalEntriesScreen(
                        onNavigateBack = { currentScreen = AppScreen.RETROSPECTIVE }
                    )
                }
            }
        }
    }
}
