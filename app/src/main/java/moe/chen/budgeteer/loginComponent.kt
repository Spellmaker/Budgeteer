package moe.chen.budgeteer

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.chen.budgeteer.data.AppDatabase
import moe.chen.budgeteer.data.BudgetEntry
import moe.chen.budgeteer.data.User
import java.time.ZonedDateTime

@Composable
fun ActualLoginManager(
    navController: NavController,
) = Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colors.background,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = AppDatabase.DB(LocalContext.current)

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val storedUser = context.findCachedUser()
        if (storedUser != null) {
            username = storedUser.username
            password = storedUser.secret
            navController.navigate("mainview")
        }
    }

    var currentMessage by remember { mutableStateOf("") }
    var debugText by remember { mutableStateOf("") }

    Column() {
        Text("Welcome to Budgeteer")
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        TextField(
            visualTransformation = PasswordVisualTransformation(),
            value = password,
            onValueChange = { password = it },
            label = { Text("Secret") }
        )
        Text(
            currentMessage ?: "null"
        )
        Row {
            Button(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    currentMessage = "in suspend block"
                    val result = db.userDao().findUser(username, password)
                    currentMessage = if (result == null) {
                        "no such user"
                    } else {
                        runBlocking {
                            context.storeUser(result)
                        }
                        result.username + " " + result.uid
                    }
                }
            }) {
                Text("Login")
            }
            Button(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    db.userDao().createUser(
                        User(
                            username = username,
                            secret = password,
                        )
                    )

                    currentMessage = "created user"
                }
            }) {
                Text("Create User")
            }
        }

        Spacer(modifier = Modifier.height(height = Dp(10f)))
        Text("Debug Part")
        Button(onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                val uid = db.userDao().findUser("Test", "Test")!!.uid!!
                val cid = db.categoryDao().listCategories(uid).first().cid!!
                db.budgetEntryDao().createEntry(BudgetEntry(
                    bid = null,
                    amount = 2.0,
                    cid = cid.toInt(),
                    date = ZonedDateTime.now()
                ))
                db.budgetEntryDao().createEntry(BudgetEntry(
                    bid = null,
                    amount = 5.0,
                    cid = cid.toInt(),
                    date = ZonedDateTime.now()
                ))
                db.budgetEntryDao().createEntry(BudgetEntry(
                    bid = null,
                    amount = 2.0,
                    cid = cid.toInt(),
                    date = ZonedDateTime.now().minusMonths(2)
                ))

                debugText = "1: ${db.budgetEntryDao().listEntries(cid.toInt(), ZonedDateTime.now().monthValue).size}\n 2: ${
                    db.budgetEntryDao().listEntries(cid.toInt(), ZonedDateTime.now().monthValue - 2).size
                }"
            }
        }) {
            Text("Do Debug")
        }

        Text(debugText)
    }

}