package com.example.conch.ui.dialog

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.DialogFragment

abstract class BaseDialog(activity: Activity) : DialogFragment() {

    fun toast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).apply {
            setText(msg)
            show()
        }
    }
}