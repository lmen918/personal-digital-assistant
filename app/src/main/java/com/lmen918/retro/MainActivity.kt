package com.lmen918.retro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.lmen918.retro.ui.journalentries.JournalEntriesScreen
import com.lmen918.retro.ui.retrospective.RetrospectiveScreen
import com.lmen918.retro.ui.settings.SettingsScreen
import com.lmen918.retro.ui.theme.retrospectiveTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
            val screens = AppScreen.entries
            val actualPageCount = screens.size
            // Use a large count to simulate infinite looping
            val initialPage = Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2 % actualPageCount)
            val pagerState = rememberPagerState(
                initialPage = initialPage,
                pageCount = { Int.MAX_VALUE }
            )
            val scope = rememberCoroutineScope()
            var settingsSavedMessage by remember { mutableStateOf<String?>(null) }

            retrospectiveTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            screens.forEachIndexed { index, screen ->
                                val isSelected = (pagerState.currentPage % actualPageCount) == index
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        scope.launch {
                                            val currentActualPage = pagerState.currentPage
                                            val targetPage = currentActualPage + (index - currentActualPage % actualPageCount)
                                            pagerState.animateScrollToPage(targetPage)
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = when (screen) {
                                                AppScreen.RETROSPECTIVE -> Icons.Filled.Home
                                                AppScreen.JOURNALS -> Icons.Filled.Book
                                                AppScreen.SETTINGS -> Icons.Filled.Settings
                                            },
                                            contentDescription = stringResource(
                                                when (screen) {
                                                    AppScreen.RETROSPECTIVE -> R.string.nav_home
                                                    AppScreen.JOURNALS -> R.string.journals
                                                    AppScreen.SETTINGS -> R.string.settings
                                                }
                                            )
                                        )
                                    },
                                    label = {
                                        Text(
                                            stringResource(
                                                when (screen) {
                                                    AppScreen.RETROSPECTIVE -> R.string.nav_home
                                                    AppScreen.JOURNALS -> R.string.journals
                                                    AppScreen.SETTINGS -> R.string.settings
                                                }
                                            )
                                        )
                                    },
                                    modifier = Modifier.testTag(
                                        when (screen) {
                                            AppScreen.RETROSPECTIVE -> NAV_HOME_TAG
                                            AppScreen.JOURNALS -> NAV_JOURNALS_TAG
                                            AppScreen.SETTINGS -> NAV_SETTINGS_TAG
                                        }
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding()),
                        beyondViewportPageCount = 1
                    ) { page ->
                        val screen = screens[page % actualPageCount]
                        when (screen) {
                            AppScreen.RETROSPECTIVE -> RetrospectiveScreen(
                                settingsSavedMessage = settingsSavedMessage,
                                onSettingsSavedMessageShown = { settingsSavedMessage = null }
                            )
                            AppScreen.JOURNALS -> JournalEntriesScreen()
                            AppScreen.SETTINGS -> SettingsScreen(
                                onSaveAndNavigateBack = { message ->
                                    settingsSavedMessage = message
                                    scope.launch {
                                        val currentActualPage = pagerState.currentPage
                                        val homeIndex = screens.indexOf(AppScreen.RETROSPECTIVE)
                                        val targetPage = currentActualPage + (homeIndex - currentActualPage % actualPageCount)
                                        pagerState.animateScrollToPage(targetPage)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
