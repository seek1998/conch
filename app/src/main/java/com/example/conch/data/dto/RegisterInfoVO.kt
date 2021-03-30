package com.example.conch.data.dto

data class RegisterInfoVO(
    var email: String = "",
    var captcha: String = "",
    var password: String = "",
    var displayName: String = ""
)