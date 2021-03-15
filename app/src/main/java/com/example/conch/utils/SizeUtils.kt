package com.example.conch.utils

object SizeUtils {

    private const val GB = 1024 * 1024 * 1024

    private const val MB = 1024 * 1024

    private const val KB = 1024

    fun byteToMbDouble(bytes: Long): Double {
        val d1 = bytes.toDouble()
        val d2 = MB.toDouble()
        return d1 / d2
    }

    fun byteToMbString(bytes: Long) = String.format("%.2f", byteToMbDouble(bytes)) + "MB"

}