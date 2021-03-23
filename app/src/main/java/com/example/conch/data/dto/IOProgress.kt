package com.example.conch.data.dto

data class IOProgress(
    val id: Long,
    var current: Long,
    val total: Long,
    var progress: Int = 0
) {
    init {
        progress = (current.div(total.toFloat()) * 1E4).toInt()
    }

    companion object {
        const val PROGRESS_MAX = 10000
    }
}