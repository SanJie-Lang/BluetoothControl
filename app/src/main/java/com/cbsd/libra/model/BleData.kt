package com.cbsd.libra.model

import com.cbsd.libra.BleAction
import com.cbsd.libra.utils.GsonUtils

data class BleData(val action: BleAction, val value: Any) {

    fun toJson(): String? {
        return GsonUtils.toJson(this)
    }

    companion object {
        fun instance(json: String): BleData {
            return GsonUtils.toObj(json, BleData::class.java)
        }
    }
}