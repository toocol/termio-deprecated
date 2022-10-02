package com.toocol.coroutines

import com.toocol.coroutines.core.AbstractApi
import javafx.scene.text.Text
import kotlinx.coroutines.delay

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 16:19
 * @version: 0.0.1
 */
class User(private val name:String, private val age: Int) {
    override fun toString(): String {
        return "{name: ${name}, age = ${age}}"
    }
}

object UserApi : AbstractApi {
    suspend fun getUser(): User {
        println("Suspend getUser: ${Thread.currentThread().name}")
        // Mock IO consumptions.
        delay(2000)
        return User("Jack", 18)
    }

    suspend fun showUser(text: Text, user: User) {
        println("Suspend showUser: ${Thread.currentThread().name}")
        // Mock UI consumptions.
        delay(2000)
        text.text = user.toString()
    }
}
