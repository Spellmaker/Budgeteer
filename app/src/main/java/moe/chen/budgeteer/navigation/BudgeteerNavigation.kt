package moe.chen.budgeteer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import moe.chen.budgeteer.screens.*
import moe.chen.budgeteer.widgets.VerifyUserData

@Composable
fun BudgeteerNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BudgeteerScreens.LoginScreen.name,
    ) {
        composable(route = BudgeteerScreens.LoginScreen.name) {
            LoginScreen(
                navController = navController,
            )
        }
        composable(
            route = "${BudgeteerScreens.OverviewScreen.name}/{user}",
            arguments = listOf(
                navArgument("user") { type = NavType.IntType },
            )
        ) {
            VerifyUserData(
                navController = navController,
            ) { user, logout ->
                OverviewScreen(navController = navController, user = user, logout = logout,
                    accessSettings = {
                        navController.navigate(
                            "${BudgeteerScreens.UserSettingsScreen.name}/${user.uid!!}"
                        )
                    })
            }
        }
        composable(
            route = "${BudgeteerScreens.AddCategoryScreen.name}/{user}/{category}",
            arguments = listOf(
                navArgument("category") { type = NavType.IntType },
                navArgument("user") { type = NavType.IntType },
            )
        ) {
            VerifyUserData(
                navController = navController,
            ) { user, logout ->
                EditCategoryScreen(navController = navController, user = user, logout = logout,
                    accessSettings = {
                        navController.navigate(
                            "${BudgeteerScreens.UserSettingsScreen.name}/${user.uid!!}"
                        )
                    })
            }
        }
        composable(
            route = "${BudgeteerScreens.CategoryScreen.name}/{user}/{category}",
            arguments = listOf(
                navArgument("category") { type = NavType.IntType },
                navArgument("user") { type = NavType.IntType },
            )
        ) {
            val category = it.arguments?.getInt("category")
            VerifyUserData(
                navController = navController,
            ) { user, logout ->
                CategoryDetailsScreen(
                    navController = navController,
                    categoryId = category!!,
                    logout = logout,
                    user = user,
                    accessSettings = {
                        navController.navigate(
                            "${BudgeteerScreens.UserSettingsScreen.name}/${user.uid!!}"
                        )
                    }
                )
            }
        }
        composable(
            route = "${BudgeteerScreens.ExpenseInputScreen.name}/{category}",
            arguments = listOf(navArgument("category") { type = NavType.IntType })
        ) {
            VerifyUserData(
                navController = navController,
            ) { user, logout ->
                InputEntryScreen(
                    navController = navController,
                    logout = logout,
                    accessSettings = {
                        navController.navigate(
                            "${BudgeteerScreens.UserSettingsScreen.name}/${user.uid!!}"
                        )
                    }
                )
            }
        }
        composable(
            route = "${BudgeteerScreens.UserSettingsScreen.name}/{user}",
            arguments = listOf(navArgument("user") { type = NavType.IntType })
        ) {
            UserSettingsScreen(navController = navController,
                accessSettings = {
                    navController.navigate(BudgeteerScreens.UserSettingsScreen.name)
                })
        }
    }
}