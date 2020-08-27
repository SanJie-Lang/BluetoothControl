package com.sanjie.videoplayer

import android.util.Log

/**
 * Created by SanJie on 2019.04.16
 * log工具.
 */
object LogUtil {
    private const val TAG = "VideoPlayer"
    @JvmStatic
    fun d(message: String?) {
        Log.d(TAG, message)
    }

    fun i(message: String?) {
        Log.i(TAG, message)
    }

    @JvmStatic
    fun e(message: String?, throwable: Throwable?) {
        Log.e(TAG, message, throwable)
    }
}