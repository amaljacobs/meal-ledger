package com.amaljacobs.mealledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
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
import com.amaljacobs.mealledger.ui.food.FoodEntryViewModel
import com.amaljacobs.mealledger.ui.food.FoodEntryFormState
import com.amaljacobs.mealledger.data.local.MealType
import com.amaljacobs.mealledger.ui.water.WaterEntryViewModel
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
            composable(AppDestination.Today.route) {
                TodayScreen(
                    repository = repository,
                    onAddFood = { navController.navigate("add-food") },
                    onAddWater = { navController.navigate("add-water") },
                )
            }
            composable("add-food") { FoodEntryScreen(repository) { navController.popBackStack() } }
            composable("add-water") { WaterEntryScreen(repository) { navController.popBackStack() } }
            composable(AppDestination.Settings.route) { EmptyScreen(message = "Settings") }
        }
    }
}

@Composable
private fun TodayScreen(
    repository: MealLedgerRepository,
    onAddFood: () -> Unit,
    onAddWater: () -> Unit,
) {
    val viewModel: TodayViewModel = viewModel(factory = TodayViewModel.factory(repository))
    val state by viewModel.uiState.collectAsState()
    when (state) {
        TodayUiState.Loading -> LoadingScreen()
        is TodayUiState.Ready -> TodayScreenContent(
            state = state as TodayUiState.Ready,
            onPreviousDay = viewModel::showPreviousDay,
            onNextDay = viewModel::showNextDay,
            onAddFood = onAddFood,
            onAddWater = onAddWater,
        )
    }
}

@Composable
fun TodayScreenContent(
    state: TodayUiState.Ready,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onAddFood: () -> Unit = {},
    onAddWater: () -> Unit = {},
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
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onAddFood) { Text("Add food") }
                Button(onClick = onAddWater) { Text("Add water") }
            }
        }
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
private fun FoodEntryScreen(repository: MealLedgerRepository, onSaved: () -> Unit) {
    val viewModel: FoodEntryViewModel = viewModel(factory = FoodEntryViewModel.factory(repository, onSaved))
    val state by viewModel.state.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Add food", style = MaterialTheme.typography.headlineSmall) }
        item { FoodField("Food name", state.name) { value -> viewModel.update { it.copy(name = value) } } }
        item { FoodField("Portion", state.portionNote) { value -> viewModel.update { it.copy(portionNote = value) } } }
        item { FoodField("Calories", state.calories) { value -> viewModel.update { it.copy(calories = value) } } }
        item { FoodField("Protein (g)", state.proteinGrams) { value -> viewModel.update { it.copy(proteinGrams = value) } } }
        item { FoodField("Price (INR)", state.price) { value -> viewModel.update { it.copy(price = value) } } }
        item { FoodField("Note", state.note) { value -> viewModel.update { it.copy(note = value) } } }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                MealType.entries.forEach { type ->
                    FilterChip(
                        selected = state.mealType == type,
                        onClick = { viewModel.update { it.copy(mealType = type) } },
                        label = { Text(type.name.lowercase().replaceFirstChar(Char::uppercase)) },
                    )
                }
            }
        }
        state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
        item { Button(onClick = viewModel::save, enabled = !state.saving) { Text(if (state.saving) "Saving" else "Save") } }
    }
}

@Composable
private fun WaterEntryScreen(repository: MealLedgerRepository, onSaved: () -> Unit) {
    val viewModel: WaterEntryViewModel = viewModel(factory = WaterEntryViewModel.factory(repository, onSaved))
    val state by viewModel.state.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Add water", style = MaterialTheme.typography.headlineSmall) }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("1 cup" to 250, "2 cups" to 500, "500 ml" to 500, "1,000 ml" to 1000).forEach { (label, amount) ->
                    FilterChip(
                        selected = state.amountMl == amount.toString(),
                        onClick = { viewModel.setAmount(amount.toString()) },
                        label = { Text(label) },
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = state.amountMl,
                onValueChange = viewModel::setAmount,
                label = { Text("Amount (ml)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
        item { Button(onClick = viewModel::save, enabled = !state.saving) { Text(if (state.saving) "Saving" else "Save") } }
    }
}

@Composable
private fun FoodField(label: String, value: String, onValueChange: (String) -> Unit) { OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = label != "Note") }

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
