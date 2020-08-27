package com.cbsd.libra.utils

import android.util.Log

object LogUtils {
    private const val TAG = "Libra"

    fun d(message: String){
        Log.d(TAG, message)
    }
}