package com.lmen918.pda.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lmen918.pda.R
import com.lmen918.pda.ui.events.EventDetailScreen
import com.lmen918.pda.ui.events.EventsScreen
import com.lmen918.pda.ui.retrospective.RetrospectiveScreen
import com.lmen918.pda.ui.tags.TagsScreen

data class BottomNavItem(
    val screen: Screen,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val labelRes: Int
)

@Composable
fun PdaNavGraph() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem(Screen.Timeline, Icons.Default.DateRange, R.string.timeline),
        BottomNavItem(Screen.Tags, Icons.Default.Label, R.string.tags),
        BottomNavItem(Screen.Retrospective, Icons.Default.Refresh, R.string.retrospective)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.labelRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Timeline.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Timeline.route) {
                EventsScreen(
                    onAddEvent = { navController.navigate(Screen.AddEditEvent.createRoute()) },
                    onEditEvent = { eventId -> navController.navigate(Screen.AddEditEvent.createRoute(eventId)) }
                )
            }
            composable(
                route = Screen.AddEditEvent.route,
                arguments = listOf(navArgument("eventId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId")?.takeIf { it != -1L }
                EventDetailScreen(
                    eventId = eventId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Tags.route) {
                TagsScreen()
            }
            composable(Screen.Retrospective.route) {
                RetrospectiveScreen()
            }
        }
    }
}
