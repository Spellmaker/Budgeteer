package moe.chen.budgeteer.widgets

import android.util.Log
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.navigation.BudgeteerScreens
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.viewmodel.UserViewModel

@Composable
fun VerifyUserData(
    navController: NavController,
    subWidget: @Composable (User, () -> Unit) -> Unit
) {
    val userViewModel = hiltViewModel<UserViewModel>()

    val logout: () -> Unit = {
        userViewModel.setActiveUser(User(null, "logout", "logout"))
        navController.navigate(BudgeteerScreens.LoginScreen.name)
    }
    val user = userViewModel.validateCurrentUser().collectAsState(initial = null)

    Log.d("VerifyUserData", "recompose for $user")

    if (user.value != null) {
        subWidget(user.value!!, logout)
    } else {
        Button(onClick = logout) {
            Text("Logout")
        }
    }
}