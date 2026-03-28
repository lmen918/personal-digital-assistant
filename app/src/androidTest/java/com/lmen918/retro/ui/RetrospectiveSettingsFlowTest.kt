package com.lmen918.retro.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.lmen918.retro.NAV_HOME_TAG
import com.lmen918.retro.NAV_SETTINGS_TAG
import com.lmen918.retro.reminder.ReminderPreferencesRepository
import com.lmen918.retro.reminder.ReminderScheduler
import com.lmen918.retro.reminder.ReminderSettings
import com.lmen918.retro.ui.retrospective.RETRO_INTRO_DESCRIPTION_TAG
import com.lmen918.retro.ui.retrospective.RetrospectiveScreen
import com.lmen918.retro.ui.retrospective.RetrospectiveViewModel
import com.lmen918.retro.ui.settings.SETTINGS_DURATION_CHIP_PREFIX
import com.lmen918.retro.ui.settings.SETTINGS_SAVE_BUTTON_TAG
import com.lmen918.retro.ui.settings.SettingsScreen
import com.lmen918.retro.ui.settings.SettingsViewModel
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

        // Navigate to settings via bottom nav
        composeRule.onNodeWithTag(NAV_SETTINGS_TAG).performClick()
        
        composeRule.onNodeWithTag("${SETTINGS_DURATION_CHIP_PREFIX}5").performClick()
        composeRule.onNodeWithTag(SETTINGS_SAVE_BUTTON_TAG).performClick()

        // After saving, it should navigate back to Retrospective automatically (as per MainActivity logic)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(RETRO_INTRO_DESCRIPTION_TAG).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(RETRO_INTRO_DESCRIPTION_TAG)
            .assertTextContains("5 minutes each")
    }

    private enum class AppScreen { RETROSPECTIVE, SETTINGS }

    private fun setTestContent() {
        composeRule.setContent {
            var currentScreen by remember { mutableStateOf(AppScreen.RETROSPECTIVE) }
            var settingsSavedMessage by remember { mutableStateOf<String?>(null) }
            val context = LocalContext.current.applicationContext
            val repository = remember { ReminderPreferencesRepository(context) }
            val scheduler = remember { ReminderScheduler(context) }
            val settingsViewModel = remember { SettingsViewModel(repository, scheduler) }
            val retrospectiveViewModel = remember { RetrospectiveViewModel(context, repository) }

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.RETROSPECTIVE,
                            onClick = { currentScreen = AppScreen.RETROSPECTIVE },
                            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                            modifier = Modifier.testTag(NAV_HOME_TAG)
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.SETTINGS,
                            onClick = { currentScreen = AppScreen.SETTINGS },
                            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            modifier = Modifier.testTag(NAV_SETTINGS_TAG)
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (currentScreen) {
                        AppScreen.RETROSPECTIVE -> RetrospectiveScreen(
                            settingsSavedMessage = settingsSavedMessage,
                            onSettingsSavedMessageShown = { settingsSavedMessage = null },
                            viewModel = retrospectiveViewModel
                        )
                        AppScreen.SETTINGS -> SettingsScreen(
                            onSaveAndNavigateBack = { message ->
                                settingsSavedMessage = message
                                currentScreen = AppScreen.RETROSPECTIVE
                            },
                            viewModel = settingsViewModel
                        )
                    }
                }
            }
        }
    }
}
