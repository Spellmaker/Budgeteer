package moe.chen.budgeteer.navigation

enum class BudgeteerScreens {
    LoginScreen,
    OverviewScreen,
    CategoryScreen,
    ExpenseInputScreen,
    AddCategoryScreen,
    UserSettingsScreen;

    companion object {
        fun fromRoute(route: String?): BudgeteerScreens = route?.let { valueOf(it) }
            ?: throw IllegalArgumentException("no such screen: $route")
    }
}