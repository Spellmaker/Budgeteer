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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import moe.chen.budgeteer.R
import moe.chen.budgeteer.navigation.BudgeteerScreens
import moe.chen.budgeteer.room.User
import moe.chen.budgeteer.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val userViewModel = hiltViewModel<UserViewModel>()
    var previousUser by remember { mutableStateOf<User?>(null) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val validUser = userViewModel.validateCurrentUser().collectAsState(initial = null)
    val context = LocalContext.current

    if (validUser.value != null) {
        Log.d("login", "valid user was found!")
        if (previousUser == null) {
            previousUser = validUser.value
            Log.d("login", "navigating due to new user ${validUser.value}")
            navController.navigate(BudgeteerScreens.OverviewScreen.name)
        } else {
            Log.d(
                "login",
                "not navigating, new user ${validUser.value} is equal to $previousUser"
            )
        }
    } else {
        Log.d("login", "valid user was not found!")
        val successString = stringResource(R.string.notify_user_created)
        val errorString = stringResource(R.string.notify_user_creation_error)
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
                            successString,
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = {
                        Toast.makeText(
                            context,
                            errorString,
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
            Text(stringResource(R.string.welcome), style = MaterialTheme.typography.caption)
            TextField(
                singleLine = true,
                value = username,
                onValueChange = updateUsername,
                label = { Text(stringResource(R.string.label_username)) }
            )
            TextField(
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                value = password,
                onValueChange = updatePassword,
                label = { Text(stringResource(R.string.label_password)) }
            )
            Row {
                Button(onClick = login) {
                    Text(stringResource(R.string.operation_login))
                }
                Button(onClick = createUser) {
                    Text(stringResource(R.string.operation_create))
                }
            }
        }
    }
}