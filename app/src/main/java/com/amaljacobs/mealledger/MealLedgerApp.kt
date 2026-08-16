package com.amaljacobs.mealledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.amaljacobs.mealledger.data.repository.MealLedgerRepository
import com.amaljacobs.mealledger.ui.today.DailyTotals
import com.amaljacobs.mealledger.ui.today.TimelineEntry
import com.amaljacobs.mealledger.ui.today.TodayUiState
import com.amaljacobs.mealledger.ui.today.TodayViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
fun MealLedgerApp(repository: MealLedgerRepository) {
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
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Today.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppDestination.Today.route) { TodayScreen(repository) }
            composable(AppDestination.Settings.route) { EmptyScreen(message = "Settings") }
        }
    }
}

@Composable
private fun TodayScreen(repository: MealLedgerRepository) {
    val viewModel: TodayViewModel = viewModel(factory = TodayViewModel.factory(repository))
    val state by viewModel.uiState.collectAsState()
    when (state) {
        TodayUiState.Loading -> LoadingScreen()
        is TodayUiState.Ready -> TodayScreenContent(
            state = state as TodayUiState.Ready,
            onPreviousDay = viewModel::showPreviousDay,
            onNextDay = viewModel::showNextDay,
        )
    }
}

@Composable
fun TodayScreenContent(
    state: TodayUiState.Ready,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DateSelector(
                date = state.selectedDate,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
            )
        }
        item { DailyTotalsRow(state.totals) }
        item {
            Text(
                text = "Activity",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (state.entries.isEmpty()) {
            item { EmptyTimeline() }
        } else {
            items(state.entries, key = { entry -> "${entry::class.simpleName}-${entry.id}" }) { entry ->
                TimelineRow(entry)
            }
        }
    }
}

@Composable
private fun DateSelector(
    date: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPreviousDay) {
            Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous day")
        }
        Text(
            text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = onNextDay) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Next day")
        }
    }
}

@Composable
private fun DailyTotalsRow(totals: DailyTotals) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TotalMetric(label = "Calories", value = totals.calories.toString())
        TotalMetric(label = "Spend", value = formatMoney(totals.spendMinor))
        TotalMetric(label = "Water", value = "${totals.waterMl} ml")
    }
}

@Composable
private fun TotalMetric(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TimelineRow(entry: TimelineEntry) {
    val time = entry.consumedAt.atZone(java.time.ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    val (icon, title, detail) = when (entry) {
        is TimelineEntry.Food -> Triple(
            Icons.Outlined.Restaurant,
            entry.entry.name,
            listOfNotNull(
                entry.entry.portionNote,
                entry.entry.calories?.let { "$it kcal" },
            ).joinToString(" | "),
        )
        is TimelineEntry.Water -> Triple(
            Icons.Outlined.WaterDrop,
            "Water",
            "${entry.entry.amountMl} ml",
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (detail.isNotBlank()) {
                Text(text = detail, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text(text = time, style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp, top = 12.dp))
}

@Composable
private fun EmptyTimeline() {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp)) {
        Text(text = "Nothing logged for this day", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Food and water entries will appear here in time order.",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun EmptyScreen(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun formatMoney(amountMinor: Long): String = "${amountMinor / 100}.${(amountMinor % 100).toString().padStart(2, '0')}"
