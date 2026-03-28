package com.lmen918.pda.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.lmen918.pda.reminder.ReminderPreferencesRepository
import com.lmen918.pda.reminder.ReminderScheduler
import com.lmen918.pda.reminder.ReminderSettings
import com.lmen918.pda.ui.retrospective.RETRO_INTRO_DESCRIPTION_TAG
import com.lmen918.pda.ui.retrospective.RETRO_SETTINGS_BUTTON_TAG
import com.lmen918.pda.ui.retrospective.RetrospectiveScreen
import com.lmen918.pda.ui.retrospective.RetrospectiveViewModel
import com.lmen918.pda.ui.settings.SETTINGS_DURATION_CHIP_PREFIX
import com.lmen918.pda.ui.settings.SETTINGS_SAVE_BUTTON_TAG
import com.lmen918.pda.ui.settings.SettingsScreen
import com.lmen918.pda.ui.settings.SettingsViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class RetrospectiveSettingsFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = ReminderPreferencesRepository(context)
        runBlocking {
            repository.save(ReminderSettings(sessionDurationMinutes = 1))
        }
    }

    @Test
    fun intro_shows_default_one_minute_duration() {
        setTestContent()

        composeRule.onNodeWithTag(RETRO_INTRO_DESCRIPTION_TAG)
            .assertTextContains("1 minute each")
    }

    @Test
    fun intro_updates_after_saving_new_duration_from_settings() {
        setTestContent()

        composeRule.onNodeWithTag(RETRO_SETTINGS_BUTTON_TAG).performClick()
        composeRule.onNodeWithTag("${SETTINGS_DURATION_CHIP_PREFIX}5").performClick()
        composeRule.onNodeWithTag(SETTINGS_SAVE_BUTTON_TAG).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(RETRO_INTRO_DESCRIPTION_TAG).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(RETRO_INTRO_DESCRIPTION_TAG)
            .assertTextContains("5 minutes each")
    }

    private fun setTestContent() {
        composeRule.setContent {
            var isSettingsScreen by remember { mutableStateOf(false) }
            var settingsSavedMessage by remember { mutableStateOf<String?>(null) }
            val context = LocalContext.current.applicationContext
            val repository = remember { ReminderPreferencesRepository(context) }
            val scheduler = remember { ReminderScheduler(context) }
            val settingsViewModel = remember { SettingsViewModel(repository, scheduler) }
            val retrospectiveViewModel = remember { RetrospectiveViewModel(context, repository) }

            if (isSettingsScreen) {
                SettingsScreen(
                    onNavigateBack = { isSettingsScreen = false },
                    onSaveAndNavigateBack = { message ->
                        settingsSavedMessage = message
                        isSettingsScreen = false
                    },
                    viewModel = settingsViewModel
                )
            } else {
                RetrospectiveScreen(
                    onOpenSettings = { isSettingsScreen = true },
                    onOpenJournals = {},
                    settingsSavedMessage = settingsSavedMessage,
                    onSettingsSavedMessageShown = { settingsSavedMessage = null },
                    viewModel = retrospectiveViewModel
                )
            }
        }
    }
}

