package com.amaljacobs.mealledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.amaljacobs.mealledger.ui.today.DailyTotals
import com.amaljacobs.mealledger.ui.today.TodayUiState
import com.amaljacobs.mealledger.ui.theme.MealLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MealLedgerTheme {
                MealLedgerApp(repository = (application as MealLedgerApplication).repository)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MealLedgerAppPreview() {
    MealLedgerTheme {
        TodayScreenContent(
            state = TodayUiState.Ready(
                selectedDate = java.time.LocalDate.now(),
                entries = emptyList(),
                totals = DailyTotals(),
            ),
            onPreviousDay = {},
            onNextDay = {},
        )
    }
}
