package com.amaljacobs.mealledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BreakfastDining
import androidx.compose.material.icons.outlined.DinnerDining
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LunchDining
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.amaljacobs.mealledger.data.repository.MealLedgerRepository
import com.amaljacobs.mealledger.data.settings.SettingsRepository
import com.amaljacobs.mealledger.data.settings.UserSettings
import com.amaljacobs.mealledger.data.goals.DailyGoalStore
import com.amaljacobs.mealledger.ui.today.DailyTotals
import com.amaljacobs.mealledger.ui.today.TimelineEntry
import com.amaljacobs.mealledger.ui.today.TodayUiState
import com.amaljacobs.mealledger.ui.today.TodayViewModel
import com.amaljacobs.mealledger.ui.food.FoodEntryViewModel
import com.amaljacobs.mealledger.ui.food.FoodEntryFormState
import com.amaljacobs.mealledger.data.local.MealType
import com.amaljacobs.mealledger.ui.water.WaterEntryViewModel
import com.amaljacobs.mealledger.ui.settings.SettingsViewModel
import com.amaljacobs.mealledger.ui.summary.WeeklyDaySummary
import com.amaljacobs.mealledger.ui.summary.WeeklySummaryUiState
import com.amaljacobs.mealledger.ui.summary.WeeklySummaryViewModel
import com.amaljacobs.mealledger.ui.summary.SummaryMode
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

    data object Summary : AppDestination {
        override val label = "Summary"
        override val icon = Icons.Outlined.DateRange
        override val route = "summary"
    }
}

private val topLevelDestinations = listOf(
    AppDestination.Today,
    AppDestination.Summary,
    AppDestination.Settings,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealLedgerApp(
    repository: MealLedgerRepository,
    settingsRepository: SettingsRepository,
    dailyGoalStore: DailyGoalStore,
) {
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
                    settingsRepository = settingsRepository,
                    dailyGoalStore = dailyGoalStore,
                    onAddFood = { navController.navigate("add-food") },
                    onAddWater = { navController.navigate("add-water") },
                    onEditFood = { id -> navController.navigate("edit-food/$id") },
                    onEditWater = { id -> navController.navigate("edit-water/$id") },
                )
            }
            composable("add-food") {
                FoodEntryScreen(repository, settingsRepository, onSaved = { navController.popBackStack() }, onNavigateBack = { navController.popBackStack() })
            }
            composable("add-water") {
                WaterEntryScreen(repository, settingsRepository, onSaved = { navController.popBackStack() }, onNavigateBack = { navController.popBackStack() })
            }
            composable(
                route = "edit-food/{entryId}",
                arguments = listOf(navArgument("entryId") { type = NavType.LongType }),
            ) { entry ->
                FoodEntryScreen(
                    repository = repository,
                    settingsRepository = settingsRepository,
                    entryId = entry.arguments?.getLong("entryId"),
                    onSaved = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(
                route = "edit-water/{entryId}",
                arguments = listOf(navArgument("entryId") { type = NavType.LongType }),
            ) { entry ->
                WaterEntryScreen(
                    repository = repository,
                    settingsRepository = settingsRepository,
                    entryId = entry.arguments?.getLong("entryId"),
                    onSaved = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(AppDestination.Settings.route) { SettingsScreen(settingsRepository, dailyGoalStore) }
            composable(AppDestination.Summary.route) {
                WeeklySummaryScreen(repository, settingsRepository, dailyGoalStore)
            }
        }
    }
}

@Composable
private fun WeeklySummaryScreen(
    repository: MealLedgerRepository,
    settingsRepository: SettingsRepository,
    dailyGoalStore: DailyGoalStore,
) {
    val viewModel: WeeklySummaryViewModel = viewModel(
        factory = WeeklySummaryViewModel.factory(repository, settingsRepository, dailyGoalStore),
    )
    when (val state = viewModel.uiState.collectAsState().value) {
        WeeklySummaryUiState.Loading -> LoadingScreen()
        is WeeklySummaryUiState.Ready -> WeeklySummaryContent(
            state = state,
            onPreviousPeriod = viewModel::showPreviousPeriod,
            onNextPeriod = viewModel::showNextPeriod,
            onModeSelected = viewModel::selectMode,
        )
    }
}

@Composable
private fun WeeklySummaryContent(
    state: WeeklySummaryUiState.Ready,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onModeSelected: (SummaryMode) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPreviousPeriod) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous ${state.period.mode.name.lowercase()}")
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (state.period.mode == SummaryMode.Week) "Week summary" else "Month summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            periodLabel(state),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    IconButton(
                        onClick = onNextPeriod,
                        enabled = state.canNavigateForward,
                    ) {
                        Icon(Icons.Outlined.ChevronRight, contentDescription = "Next ${state.period.mode.name.lowercase()}")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.period.mode == SummaryMode.Week,
                        onClick = { onModeSelected(SummaryMode.Week) },
                        label = { Text("Week") },
                    )
                    FilterChip(
                        selected = state.period.mode == SummaryMode.Month,
                        onClick = { onModeSelected(SummaryMode.Month) },
                        label = { Text("Month") },
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TotalMetric("Calories", state.totalCalories.toString(), Modifier.weight(1f))
                TotalMetric("Food spend", formatMoney(state.totalSpendMinor, state.settings.currencyCode), Modifier.weight(1f))
                TotalMetric("Avg. water", "${state.averageWaterMl} ml", Modifier.weight(1f))
            }
        }
        item {
            TotalMetric(
                "Protein",
                "${state.totalProteinGrams} g",
                Modifier.padding(horizontal = 20.dp),
            )
        }
        item {
            GoalAttainment(state)
        }
        if (state.period.mode == SummaryMode.Month) {
            item { MonthlyCalendar(state) }
        } else {
            item {
                Text(
                    "Daily activity",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(state.days, key = { it.date }) { day -> WeeklyDayRow(day, state.settings) }
        }
    }
}

@Composable
private fun GoalAttainment(state: WeeklySummaryUiState.Ready) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            "Water goal: ${state.daysAtWaterGoal} of ${state.days.size} days",
            style = MaterialTheme.typography.bodyMedium,
        )
        state.daysAtCalorieGoal?.let {
            Text("Calorie goal: $it of ${state.calorieGoalDayCount} days", style = MaterialTheme.typography.bodyMedium)
        }
        state.daysAtProteinGoal?.let {
            Text("Protein goal: $it of ${state.proteinGoalDayCount} days", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun periodLabel(state: WeeklySummaryUiState.Ready): String = if (state.period.mode == SummaryMode.Month) {
    state.period.startDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
} else {
    "${state.period.startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))} - ${state.period.endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))}"
}

@Composable
private fun MonthlyCalendar(state: WeeklySummaryUiState.Ready) {
    var selectedMetric by remember { mutableStateOf(MonthlyMetric.Calories) }
    val leadingEmptyDays = state.period.startDate.dayOfWeek.value - 1
    val calendarDays = List<WeeklyDaySummary?>(leadingEmptyDays) { null } + state.days
    Column(modifier = Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Daily activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MonthlyMetric.entries.forEach { metric ->
                FilterChip(
                    selected = selectedMetric == metric,
                    onClick = { selectedMetric = metric },
                    label = { Text(metric.label) },
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        calendarDays.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { day ->
                    MonthlyDayCell(day, selectedMetric, state.settings, Modifier.weight(1f))
                }
                repeat(7 - week.size) { MonthlyDayCell(null, selectedMetric, state.settings, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun MonthlyDayCell(
    day: WeeklyDaySummary?,
    metric: MonthlyMetric,
    settings: UserSettings,
    modifier: Modifier = Modifier,
) {
    val cellShape = RoundedCornerShape(4.dp)
    val hasActivity = day?.entryCount?.let { it > 0 } == true
    Column(
        modifier = modifier
            .aspectRatio(0.9f)
            .background(
                color = if (hasActivity) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
                shape = cellShape,
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, cellShape)
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(day?.date?.dayOfMonth?.toString().orEmpty(), style = MaterialTheme.typography.labelMedium)
        if (day != null && day.entryCount > 0) {
            Text(
                metric.format(day, settings),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private enum class MonthlyMetric(val label: String) {
    Calories("Calories"),
    Water("Water"),
    Spend("Spend");

    fun format(day: WeeklyDaySummary, settings: UserSettings): String = when (this) {
        Calories -> "${day.calories} kcal"
        Water -> "${day.waterMl} ml"
        Spend -> formatMoney(day.spendMinor, settings.currencyCode)
    }
}

@Composable
private fun WeeklyDayRow(day: WeeklyDaySummary, settings: UserSettings) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(day.date.format(DateTimeFormatter.ofPattern("EEE, MMM d")), fontWeight = FontWeight.Medium)
            Text(
                if (day.entryCount == 0) "No entries" else "${day.calories} kcal | ${day.waterMl} ml",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(formatMoney(day.spendMinor, settings.currencyCode), style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp))
}

@Composable
private fun TodayScreen(
    repository: MealLedgerRepository,
    settingsRepository: SettingsRepository,
    dailyGoalStore: DailyGoalStore,
    onAddFood: () -> Unit,
    onAddWater: () -> Unit,
    onEditFood: (Long) -> Unit,
    onEditWater: (Long) -> Unit,
) {
    val viewModel: TodayViewModel = viewModel(
        factory = TodayViewModel.factory(repository, settingsRepository, dailyGoalStore),
    )
    val state by viewModel.uiState.collectAsState()
    when (state) {
        TodayUiState.Loading -> LoadingScreen()
        is TodayUiState.Ready -> TodayScreenContent(
            state = state as TodayUiState.Ready,
            onPreviousDay = viewModel::showPreviousDay,
            onNextDay = viewModel::showNextDay,
            onAddFood = onAddFood,
            onAddWater = onAddWater,
            onEditFood = onEditFood,
            onEditWater = onEditWater,
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
    onEditFood: (Long) -> Unit = {},
    onEditWater: (Long) -> Unit = {},
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
        item { DailyTotalsRow(state.totals, state.settings, state.goal) }
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
                TimelineRow(
                    entry = entry,
                    onClick = {
                        when (entry) {
                            is TimelineEntry.Food -> onEditFood(entry.id)
                            is TimelineEntry.Water -> onEditWater(entry.id)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun FoodEntryScreen(
    repository: MealLedgerRepository,
    settingsRepository: SettingsRepository,
    entryId: Long? = null,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val viewModel: FoodEntryViewModel = viewModel(
        factory = FoodEntryViewModel.factory(repository, settingsRepository, onSaved, entryId),
    )
    val state by viewModel.state.collectAsState()
    val settings by settingsRepository.settings.collectAsState(initial = UserSettings())
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
                Text(if (entryId == null) "Add food" else "Edit food", style = MaterialTheme.typography.headlineSmall)
            }
        }
        item { FoodField("Food name", state.name) { value -> viewModel.update { it.copy(name = value) } } }
        item { FoodField("Portion", state.portionNote) { value -> viewModel.update { it.copy(portionNote = value) } } }
        item { FoodField("Calories", state.calories) { value -> viewModel.update { it.copy(calories = value) } } }
        item { FoodField("Protein (g)", state.proteinGrams) { value -> viewModel.update { it.copy(proteinGrams = value) } } }
        item { FoodField("Price (${settings.currencyCode})", state.price) { value -> viewModel.update { it.copy(price = value) } } }
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
        item { Button(onClick = viewModel::save, enabled = !state.saving && !state.loading) { Text(if (state.saving) "Saving" else "Save") } }
        if (entryId != null) {
            item { OutlinedButton(onClick = { showDeleteConfirmation = true }, enabled = !state.saving && !state.loading) { Text("Delete") } }
        }
    }
    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            entryLabel = "food entry",
            onConfirm = viewModel::delete,
            onDismiss = { showDeleteConfirmation = false },
        )
    }
}

@Composable
private fun WaterEntryScreen(
    repository: MealLedgerRepository,
    settingsRepository: SettingsRepository,
    entryId: Long? = null,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val viewModel: WaterEntryViewModel = viewModel(factory = WaterEntryViewModel.factory(repository, onSaved, entryId))
    val state by viewModel.state.collectAsState()
    val settings by settingsRepository.settings.collectAsState(initial = UserSettings())
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
                Text(if (entryId == null) "Add water" else "Edit water", style = MaterialTheme.typography.headlineSmall)
            }
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "1 cup" to settings.cupSizeMl,
                    "2 cups" to settings.cupSizeMl * 2,
                    "500 ml" to 500,
                    "1,000 ml" to 1_000,
                ).forEach { (label, amount) ->
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
        item { Button(onClick = viewModel::save, enabled = !state.saving && !state.loading) { Text(if (state.saving) "Saving" else "Save") } }
        if (entryId != null) {
            item { OutlinedButton(onClick = { showDeleteConfirmation = true }, enabled = !state.saving && !state.loading) { Text("Delete") } }
        }
    }
    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            entryLabel = "water entry",
            onConfirm = viewModel::delete,
            onDismiss = { showDeleteConfirmation = false },
        )
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
        IconButton(onClick = onNextDay, enabled = date.isBefore(LocalDate.now())) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Next day")
        }
    }
}

@Composable
private fun DailyTotalsRow(totals: DailyTotals, settings: UserSettings, goal: com.amaljacobs.mealledger.data.goals.DailyGoal) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            GoalTotalMetric("Calories", totals.calories, goal.calories, "kcal", Modifier.weight(1f))
            TotalMetric(
                label = "Spend",
                value = formatMoney(totals.spendMinor, settings.currencyCode),
                modifier = Modifier.weight(1f),
            )
            WaterTotalMetric(totals.waterMl, goal.waterMl)
        }
        if (goal.proteinGrams != null) {
            GoalTotalMetric("Protein", totals.proteinGrams, goal.proteinGrams, "g", Modifier.padding(horizontal = 20.dp))
        }
    }
}

@Composable
private fun GoalTotalMetric(label: String, value: Int, goal: Int?, unit: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(text = "$value $unit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        goal?.let {
            LinearProgressIndicator(progress = { (value.toFloat() / it).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
            Text(text = "/ $it $unit", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun WaterTotalMetric(waterMl: Int, dailyGoalMl: Int) {
    val progress = (waterMl.toFloat() / dailyGoalMl).coerceIn(0f, 1f)
    Column(modifier = Modifier.size(width = 104.dp, height = 64.dp)) {
        Text(text = "Water", style = MaterialTheme.typography.labelLarge)
        Text(text = "$waterMl ml", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        Text(text = "/ $dailyGoalMl ml", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TotalMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TimelineRow(entry: TimelineEntry, onClick: () -> Unit) {
    val time = entry.consumedAt.atZone(java.time.ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    val (icon, title, detail) = when (entry) {
        is TimelineEntry.Food -> Triple(
            foodIcon(entry.entry.mealType),
            entry.entry.name,
            listOfNotNull(
                entry.entry.portionNote,
                entry.entry.calories?.let { "$it kcal" },
                entry.entry.proteinGrams?.let { "${it}g protein" },
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
            .clickable(onClick = onClick)
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

private fun foodIcon(mealType: MealType?): ImageVector = when (mealType) {
    MealType.BREAKFAST -> Icons.Outlined.BreakfastDining
    MealType.LUNCH -> Icons.Outlined.LunchDining
    MealType.DINNER -> Icons.Outlined.DinnerDining
    MealType.SNACK -> Icons.Outlined.Fastfood
    MealType.OTHER, null -> Icons.Outlined.Restaurant
}

@Composable
private fun DeleteConfirmationDialog(
    entryLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete entry?") },
        text = { Text("This $entryLabel will be permanently removed.") },
        confirmButton = { Button(onClick = onConfirm) { Text("Delete") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(settingsRepository: SettingsRepository, dailyGoalStore: DailyGoalStore) {
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(settingsRepository, dailyGoalStore))
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var currencyMenuExpanded by remember { mutableStateOf(false) }
    val currencies = listOf("INR", "USD", "EUR", "GBP", "AED")

    LaunchedEffect(state.saveConfirmationId) {
        if (state.saveConfirmationId > 0) {
            snackbarHostState.showSnackbar("Settings saved")
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(contentPadding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        item { Text("Preferences", style = MaterialTheme.typography.headlineSmall) }
        item {
            ExposedDropdownMenuBox(
                expanded = currencyMenuExpanded,
                onExpandedChange = { currencyMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = state.currencyCode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Currency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(currencyMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = currencyMenuExpanded,
                    onDismissRequest = { currencyMenuExpanded = false },
                ) {
                    currencies.forEach { currencyCode ->
                        DropdownMenuItem(
                            text = { Text(currencyCode) },
                            onClick = {
                                viewModel.update { it.copy(currencyCode = currencyCode) }
                                currencyMenuExpanded = false
                            },
                        )
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = state.dailyWaterGoalMl,
                onValueChange = { value -> viewModel.update { it.copy(dailyWaterGoalMl = value) } },
                label = { Text("Daily water goal (ml)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = state.dailyCalorieGoal,
                onValueChange = { value -> viewModel.update { it.copy(dailyCalorieGoal = value) } },
                label = { Text("Daily calorie goal (kcal, optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = state.dailyProteinGoalGrams,
                onValueChange = { value -> viewModel.update { it.copy(dailyProteinGoalGrams = value) } },
                label = { Text("Daily protein goal (g, optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = state.cupSizeMl,
                onValueChange = { value -> viewModel.update { it.copy(cupSizeMl = value) } },
                label = { Text("Default cup size (ml)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
        item {
            Button(
                onClick = viewModel::save,
                enabled = !state.loading && !state.saving,
            ) { Text(if (state.saving) "Saving" else "Save settings") }
        }
        }
    }
}

private fun formatMoney(amountMinor: Long, currencyCode: String): String =
    "$currencyCode ${amountMinor / 100}.${(amountMinor % 100).toString().padStart(2, '0')}"
