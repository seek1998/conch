package com.example.conch.data


sealed class MyResult<out T : Any> {

    data class Success<out T : Any>(val data: T?) : MyResult<T>()

    data class Error(val exception: Exception) : MyResult<Nothing>()

    data class Loading(val progress: Int?) : MyResult<Int>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Loading -> "Loading[progress=$progress]"
            is Error -> "Error[exception=$exception]"
        }
    }
}