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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
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
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
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
                    onAddFood = { date -> navController.navigate("add-food/$date") },
                    onAddWater = { date -> navController.navigate("add-water/$date") },
                    onEditFood = { id -> navController.navigate("edit-food/$id") },
                    onEditWater = { id -> navController.navigate("edit-water/$id") },
                )
            }
            composable("add-food/{selectedDate}") { entry ->
                FoodEntryScreen(
                    repository = repository,
                    settingsRepository = settingsRepository,
                    selectedDate = entry.arguments?.getString("selectedDate")?.let(LocalDate::parse),
                    onSaved = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable("add-water/{selectedDate}") { entry ->
                WaterEntryScreen(
                    repository = repository,
                    settingsRepository = settingsRepository,
                    selectedDate = entry.arguments?.getString("selectedDate")?.let(LocalDate::parse),
                    onSaved = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() },
                )
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
    onAddFood: (LocalDate) -> Unit,
    onAddWater: (LocalDate) -> Unit,
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
            onDateSelected = viewModel::selectDate,
            onAddFood = { onAddFood((state as TodayUiState.Ready).selectedDate) },
            onAddWater = { onAddWater((state as TodayUiState.Ready).selectedDate) },
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
    onDateSelected: (LocalDate) -> Unit = {},
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
                onDateSelected = onDateSelected,
            )
        }
        item { DailyTotalsRow(state.totals, state.settings, state.goal) }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onAddFood, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Restaurant, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Add food")
                }
                OutlinedButton(onClick = onAddWater, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.WaterDrop, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Add water")
                }
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
            item { EmptyTimeline(onAddFood = onAddFood, onAddWater = onAddWater) }
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
    selectedDate: LocalDate? = null,
    entryId: Long? = null,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val viewModel: FoodEntryViewModel = viewModel(
        factory = FoodEntryViewModel.factory(repository, settingsRepository, onSaved, entryId, selectedDate),
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
        item { Text("Optional details", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
        item { FoodField("Portion", state.portionNote, optional = true) { value -> viewModel.update { it.copy(portionNote = value) } } }
        item { FoodField("Calories", state.calories, numeric = true, optional = true) { value -> viewModel.update { it.copy(calories = value) } } }
        item { FoodField("Protein (g)", state.proteinGrams, numeric = true, optional = true) { value -> viewModel.update { it.copy(proteinGrams = value) } } }
        item { FoodField("Price (${settings.currencyCode})", state.price, numeric = true, optional = true) { value -> viewModel.update { it.copy(price = value) } } }
        item { FoodField("Note", state.note, optional = true) { value -> viewModel.update { it.copy(note = value) } } }
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
    selectedDate: LocalDate? = null,
    entryId: Long? = null,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val viewModel: WaterEntryViewModel = viewModel(
        factory = WaterEntryViewModel.factory(repository, onSaved, entryId, selectedDate),
    )
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
private fun FoodField(
    label: String,
    value: String,
    numeric: Boolean = false,
    optional: Boolean = false,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(if (optional) "$label (optional)" else label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = label != "Note",
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text),
    )
}

@Composable
private fun DateSelector(
    date: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
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
        OutlinedButton(onClick = { showDatePicker = true }) {
            Text(
                text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        IconButton(onClick = onNextDay, enabled = date.isBefore(LocalDate.now())) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Next day")
        }
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            onDateSelected(Instant.ofEpochMilli(selectedMillis).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                        showDatePicker = false
                    },
                ) { Text("Select") }
            },
            dismissButton = { OutlinedButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DailyTotalsRow(totals: DailyTotals, settings: UserSettings, goal: com.amaljacobs.mealledger.data.goals.DailyGoal) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LedgerMetricCard(
                label = "Calories",
                value = totals.calories,
                unit = "kcal",
                goal = goal.calories,
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            LedgerMetricCard(
                label = "Water",
                value = totals.waterMl,
                unit = "ml",
                goal = goal.waterMl,
                accent = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LedgerMetricCard(
                label = "Food spend",
                displayValue = formatMoney(totals.spendMinor, settings.currencyCode),
                accent = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
            )
            if (goal.proteinGrams != null) {
                LedgerMetricCard(
                    label = "Protein",
                    value = totals.proteinGrams,
                    unit = "g",
                    goal = goal.proteinGrams,
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            } else {
                LedgerMetricCard(
                    label = "Protein",
                    displayValue = "Not set",
                    supportingText = "Add a goal in Settings",
                    accent = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LedgerMetricCard(
    label: String,
    value: Int? = null,
    unit: String? = null,
    goal: Int? = null,
    displayValue: String? = null,
    supportingText: String? = null,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(126.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = accent)
            Text(
                text = displayValue ?: "$value $unit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (goal != null && value != null && unit != null) {
                SteppedProgress(
                    progress = (value.toFloat() / goal).coerceIn(0f, 1f),
                    color = accent,
                )
                Text("Goal $goal $unit", style = MaterialTheme.typography.labelSmall)
            } else {
                Text(supportingText.orEmpty(), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun SteppedProgress(progress: Float, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        val completedSteps = (progress * 10).toInt()
        repeat(10) { index ->
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .background(
                        color = if (index < completedSteps) color else MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(1.dp),
                    ),
            )
        }
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
    val accent = if (entry is TimelineEntry.Water) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                if (detail.isNotBlank()) {
                    Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(text = time, style = MaterialTheme.typography.labelMedium, color = accent)
        }
    }
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
private fun EmptyTimeline(onAddFood: () -> Unit, onAddWater: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
        Text(text = "[  ]", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = "Nothing logged for this day", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Food and water entries will appear here in time order.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAddFood, modifier = Modifier.weight(1f)) { Text("Add food") }
            OutlinedButton(onClick = onAddWater, modifier = Modifier.weight(1f)) { Text("Add water") }
        }
        }
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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var currencyMenuExpanded by remember { mutableStateOf(false) }
    val currencies = listOf("INR", "USD", "EUR", "GBP", "AED")

    DisposableEffect(snackbarHostState) {
        onDispose {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    LaunchedEffect(state.saveConfirmationId) {
        val confirmationId = state.saveConfirmationId
        if (confirmationId > 0) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            try {
                val snackbar = launch {
                    snackbarHostState.showSnackbar(
                        message = "Settings saved",
                        duration = SnackbarDuration.Indefinite,
                    )
                }
                delay(2_000)
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbar.join()
            } finally {
                viewModel.consumeSaveConfirmation(confirmationId)
            }
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
        item { Text("Daily targets", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
        item {
            OutlinedTextField(
                value = state.dailyWaterGoalMl,
                onValueChange = { value -> viewModel.update { it.copy(dailyWaterGoalMl = value) } },
                label = { Text("Daily water goal (ml)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        item {
            OutlinedTextField(
                value = state.dailyCalorieGoal,
                onValueChange = { value -> viewModel.update { it.copy(dailyCalorieGoal = value) } },
                label = { Text("Daily calorie goal (kcal, optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        item {
            OutlinedTextField(
                value = state.dailyProteinGoalGrams,
                onValueChange = { value -> viewModel.update { it.copy(dailyProteinGoalGrams = value) } },
                label = { Text("Daily protein goal (g, optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        item { Text("Water", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
        item {
            OutlinedTextField(
                value = state.cupSizeMl,
                onValueChange = { value -> viewModel.update { it.copy(cupSizeMl = value) } },
                label = { Text("Default cup size (ml)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
