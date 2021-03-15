package com.example.conch.data.dto

data class STSToken(
    var expiration: String = "",
    var accessKeyId: String = "",
    var accessKeySecret: String = "",
    var securityToken: String = "",
    var requestId: String = ""
)
