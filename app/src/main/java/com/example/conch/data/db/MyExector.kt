package com.example.conch.data.db

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object MyExecutor {
    val myExecutor: ExecutorService = Executors.newSingleThreadExecutor()
}
