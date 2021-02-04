package com.example.conch.utils

object RegexUtil {

    private val emailRegex = """^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+${'$'}""".toRegex()

    fun isEmail(string: String) = emailRegex.containsMatchIn(string)
}