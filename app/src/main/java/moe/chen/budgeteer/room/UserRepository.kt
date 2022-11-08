package moe.chen.budgeteer.room

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UserRepository @Inject constructor(private val userDao: UserDao) {

    fun findUser(name: String, secret: String): Flow<User?> =
        userDao.findUser(name, secret)
            .flowOn(Dispatchers.IO)
            .conflate()

    suspend fun createUser(user: User): Long = userDao.createUser(user)
}