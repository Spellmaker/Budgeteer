package moe.chen.budgeteer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import moe.chen.budgeteer.screens.*

@Composable
fun BudgeteerNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BudgeteerScreens.OverviewScreen.name,
    ) {
        composable(
            route = BudgeteerScreens.OverviewScreen.name,
        ) {
            OverviewScreen(navController = navController)
        }
        composable(
            route = "${BudgeteerScreens.AddCategoryScreen.name}/{user}/{category}",
            arguments = listOf(
                navArgument("category") { type = NavType.IntType },
                navArgument("user") { type = NavType.IntType },
            )
        ) {
            EditCategoryScreen(navController = navController)
        }
        composable(
            route = "${BudgeteerScreens.CategoryScreen.name}/{user}/{category}",
            arguments = listOf(
                navArgument("category") { type = NavType.IntType },
                navArgument("user") { type = NavType.IntType },
            )
        ) {
            val category = it.arguments?.getInt("category")
            CategoryDetailsScreen(
                navController = navController,
                categoryId = category!!,
            )
        }
        composable(
            route = "${BudgeteerScreens.ExpenseInputScreen.name}/{category}",
            arguments = listOf(navArgument("category") { type = NavType.IntType })
        ) {
            InputEntryScreen(
                navController = navController,
            )
        }
        composable(
            route = "${BudgeteerScreens.UserSettingsScreen.name}/{user}",
            arguments = listOf(navArgument("user") { type = NavType.IntType })
        ) {
            UserSettingsScreen(navController = navController)
        }
    }
}