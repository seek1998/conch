package com.example.conch.extension

import java.util.*

fun Int.getFormattedDuration(forceShowHours: Boolean = false): String {
    val builder = StringBuilder(8)
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60

    if (this >= 3600) {
        builder.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    } else if (forceShowHours) {
        builder.append("0:")
    }

    builder.append(String.format(Locale.getDefault(), "%02d", minutes))

    builder.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))

    return builder.toString()
}