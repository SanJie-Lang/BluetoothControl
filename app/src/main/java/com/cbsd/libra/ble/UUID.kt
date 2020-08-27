package com.cbsd.libra.ble

import java.util.UUID

object UUID {

    //特定的
    const val NotificationDescriptorUUID = "00002902-0000-1000-8000-00805f9b34fb"
    //服务
    val SERVICE_UUID: UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
    val SERVICE_UUID_2: UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")
    //特征
    val CHARACTERISTIC_UUID: UUID = UUID.fromString("0000ff11-0000-1000-8000-00805f9b34fb")
    val CHARACTERISTIC_UUID_2: UUID = UUID.fromString("0000ff12-0000-1000-8000-00805f9b34fb")
    //描述
    val DESCRIPTOR_UUID: UUID = UUID.fromString(NotificationDescriptorUUID)
}