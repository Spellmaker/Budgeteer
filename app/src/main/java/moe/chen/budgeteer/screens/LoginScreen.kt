package moe.chen.budgeteer.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import moe.chen.budgeteer.navigation.BudgeteerScreens
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel,
    navController: NavController,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val validUser = userViewModel.validUser.collectAsState()
    val context = LocalContext.current

    if (validUser.value != null) {
        Log.d("login", "valid user was found!")
        navController.navigate(BudgeteerScreens.OverviewScreen.name)
    } else {
        Log.d("login", "valid user was not found!")
        LoginWidget(
            modifier = Modifier,
            username = username,
            password = password,
            updateUsername = { username = it },
            updatePassword = { password = it },
            login = {
                Log.d("login", "clicked on login button, setting to $username, $password")
                userViewModel.setActiveUser(User(null, username, password))
            },
            createUser = {
                userViewModel.addUser(
                    user = User(null, username, password),
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "User created with id $it",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = {
                        Toast.makeText(
                            context,
                            "User was not created, maybe username is already taken",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun LoginWidget(
    modifier: Modifier = Modifier,
    username: String = "",
    password: String = "",
    updateUsername: (String) -> Unit = {},
    updatePassword: (String) -> Unit = {},
    login: () -> Unit = {},
    createUser: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colors.background,
    ) {
        Column {
            Text("Welcome to Budgeteer", style = MaterialTheme.typography.caption)
            TextField(
                singleLine = true,
                value = username,
                onValueChange = updateUsername,
                label = { Text("Username") }
            )
            TextField(
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                value = password,
                onValueChange = updatePassword,
                label = { Text("Secret") }
            )
            Row {
                Button(onClick = login) {
                    Text("Login")
                }
                Button(onClick = createUser) {
                    Text("Create User")
                }
            }
        }
    }
}