package com.example.conch

import com.example.conch.data.model.User
import com.example.conch.data.net.ApiResponse
import com.example.conch.data.net.ServiceCreator
import com.example.conch.data.net.api.UserService
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    val userService = ServiceCreator.create(UserService::class.java)
    val user = User(1 ,"1309882195@qq.com", "nognago", "fgagfa")

//    @Test
//    fun login_test() {
//
//        GlobalScope.launch {
//            try {
//                val response = userService.login("1234@qq.com", "1234").execute()
//                println(response.toString())
//                println(response.body())
//            } catch (e: IOException) {
//                println(e.message)
//            }
//
//        }
//
//        Thread.sleep(3000)
//    }

    @Test
    fun login_test2() {
        GlobalScope.launch {
            val apiResponse =  userService.login("1234@qq.com", "1234")
            val user = apiResponse.data
            println(apiResponse.code)
            println(apiResponse.message)
            println(user)
        }

        Thread.sleep(3000)

    }

    @Test
    fun test3() {
        val json =
            """{"code":10000,"message":"成功","data":{"uid":11,"email":"1234@qq.com","name":"任东鸣","password":"1234"}}"""
        val gson = GsonBuilder()
            .serializeNulls()
            .create()
        val resultType = object : TypeToken<ApiResponse<User>>() {}.type

        var apiResponse = gson.fromJson<ApiResponse<User>>(json, resultType)
        println(apiResponse.data?.email)
    }


}