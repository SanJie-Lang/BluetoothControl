package com.cbsd.libra.utils

import com.google.gson.Gson

object GsonUtils {

    fun toJson(obj: Any?): String? {
        return Gson().toJson(obj)
    }

    fun <T> toObj(json: String?, cls: Class<T>?): T {
        return Gson().fromJson(json, cls)
    }

    fun <T> toObj(obj: Any?, cls: Class<T>?): T {
        return Gson().fromJson(toJson(obj), cls)
    }
}