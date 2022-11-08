package moe.chen.budgeteer.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import moe.chen.budgeteer.screens.*
import moe.chen.budgeteer.viewmodel.UserViewModel
import moe.chen.budgeteer.widgets.VerifyUserData

@Composable
fun BudgeteerNavigation() {
    val navController = rememberNavController()
    val authModel = hiltViewModel<UserViewModel>()

    NavHost(
        navController = navController,
        startDestination = BudgeteerScreens.LoginScreen.name,
    ) {
        composable(route = BudgeteerScreens.LoginScreen.name) {
            LoginScreen(
                navController = navController,
                userViewModel = authModel,
            )
        }
        composable(route = BudgeteerScreens.OverviewScreen.name) {
            VerifyUserData(
                navController = navController,
                userViewModel = authModel
            ) { user, logout ->
                OverviewScreen(navController = navController, user = user, logout = logout)
            }
        }
        composable(route = BudgeteerScreens.AddCategoryScreen.name) {
            VerifyUserData(
                navController = navController,
                userViewModel = authModel
            ) { user, logout ->
                EditCategoryScreen(navController = navController, user = user, logout = logout)
            }
        }
        composable(
            route = "${BudgeteerScreens.CategoryScreen.name}/{category}",
            arguments = listOf(navArgument("category") { type = NavType.IntType })
        ) {
            val category = it.arguments?.getInt("category")
            VerifyUserData(
                navController = navController,
                userViewModel = authModel
            ) { _, logout ->
                CategoryDetailsScreen(
                    navController = navController,
                    categoryId = category!!,
                    logout = logout
                )
            }
        }
        composable(
            route = "${BudgeteerScreens.ExpenseInputScreen.name}/{category}",
            arguments = listOf(navArgument("category") { type = NavType.IntType })
        ) {
            val category = it.arguments?.getInt("category")
            VerifyUserData(
                navController = navController,
                userViewModel = authModel
            ) { _, logout ->
                InputEntryScreen(
                    navController = navController,
                    categoryId = category!!,
                    logout = logout
                )
            }
        }
    }
}