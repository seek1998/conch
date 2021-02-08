package com.example.conch.data.model

data class RegisterInfoVO(
    var email: String = "",
    var captcha: String = "",
    var password: String = "",
    var displayName: String = ""
)