package moe.chen.budgeteer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import moe.chen.budgeteer.navigation.BudgeteerNavigation
import moe.chen.budgeteer.ui.theme.BudgeteerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BudgeteerTheme {
                BudgeteerNavigation()
            }
        }
    }
}