package com.amaljacobs.mealledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed interface AppDestination {
    val label: String
    val icon: ImageVector
    val route: String

    data object Today : AppDestination {
        override val label = "Today"
        override val icon = Icons.Outlined.Home
        override val route = "today"
    }

    data object Settings : AppDestination {
        override val label = "Settings"
        override val icon = Icons.Outlined.Settings
        override val route = "settings"
    }
}

private val topLevelDestinations = listOf(AppDestination.Today, AppDestination.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealLedgerApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentLabel = topLevelDestinations.firstOrNull { destination ->
        currentDestination?.route == destination.route
    }?.label ?: "Meal Ledger"

    Scaffold(
        topBar = { TopAppBar(title = { Text(currentLabel) }) },
        bottomBar = {
            NavigationBar {
                topLevelDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.route == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(AppDestination.Today.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestination.Today.route) { TodayScreen() }
            composable(AppDestination.Settings.route) { SettingsScreen() }
        }
    }
}

@Composable
private fun TodayScreen() {
    EmptyScreen(message = "No entries for today")
}

@Composable
private fun SettingsScreen() {
    EmptyScreen(message = "Settings")
}

@Composable
private fun EmptyScreen(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingValues(horizontal = 24.dp, vertical = 32.dp)),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}
