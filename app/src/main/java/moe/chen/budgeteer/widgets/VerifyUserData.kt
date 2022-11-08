package moe.chen.budgeteer.widgets

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import moe.chen.budgeteer.navigation.BudgeteerScreens
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.viewmodel.UserViewModel

@Composable
fun VerifyUserData(
    navController: NavController,
    userViewModel: UserViewModel,
    subWidget: @Composable (User, () -> Unit) -> Unit
) {
    val logout = {
        userViewModel.setActiveUser(User(null, "logout", "logout"))
        navController.navigate(BudgeteerScreens.LoginScreen.name)
    }
    val user = userViewModel.validUser.collectAsState().value
    if (user != null) {
        subWidget(user, logout)
    } else {
        Button(onClick = logout) {
            Text("Logout")
        }
    }
}