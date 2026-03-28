package com.lmen918.pda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.lmen918.pda.ui.journalentries.JournalEntriesScreen
import com.lmen918.pda.ui.retrospective.RetrospectiveScreen
import com.lmen918.pda.ui.settings.SettingsScreen
import com.lmen918.pda.ui.theme.PdaTheme
import dagger.hilt.android.AndroidEntryPoint

const val NAV_HOME_TAG = "nav_home_tab"
const val NAV_JOURNALS_TAG = "nav_journals_tab"
const val NAV_SETTINGS_TAG = "nav_settings_tab"

private enum class AppScreen { RETROSPECTIVE, JOURNALS, SETTINGS }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentScreen by remember { mutableStateOf(AppScreen.RETROSPECTIVE) }
            var settingsSavedMessage by remember { mutableStateOf<String?>(null) }
            PdaTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.RETROSPECTIVE,
                                onClick = { currentScreen = AppScreen.RETROSPECTIVE },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Home,
                                        contentDescription = stringResource(R.string.nav_home)
                                    )
                                },
                                label = { Text(stringResource(R.string.nav_home)) },
                                modifier = Modifier.testTag(NAV_HOME_TAG)
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.JOURNALS,
                                onClick = { currentScreen = AppScreen.JOURNALS },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Book,
                                        contentDescription = stringResource(R.string.journals)
                                    )
                                },
                                label = { Text(stringResource(R.string.journals)) },
                                modifier = Modifier.testTag(NAV_JOURNALS_TAG)
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.SETTINGS,
                                onClick = { currentScreen = AppScreen.SETTINGS },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = stringResource(R.string.settings)
                                    )
                                },
                                label = { Text(stringResource(R.string.settings)) },
                                modifier = Modifier.testTag(NAV_SETTINGS_TAG)
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        when (currentScreen) {
                            AppScreen.RETROSPECTIVE -> RetrospectiveScreen(
                                settingsSavedMessage = settingsSavedMessage,
                                onSettingsSavedMessageShown = { settingsSavedMessage = null }
                            )
                            AppScreen.SETTINGS -> SettingsScreen(
                                onSaveAndNavigateBack = { message ->
                                    settingsSavedMessage = message
                                    currentScreen = AppScreen.RETROSPECTIVE
                                }
                            )
                            AppScreen.JOURNALS -> JournalEntriesScreen()
                        }
                    }
                }
            }
        }
    }
}
