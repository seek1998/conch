package com.example.conch.data.remote

data class ApiResponse<T>(
    var code: Int = 0,
    var message: String = "",
    var data: T?
)