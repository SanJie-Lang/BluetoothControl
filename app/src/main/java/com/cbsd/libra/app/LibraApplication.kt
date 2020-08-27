package com.cbsd.libra.app

import android.app.Application
import com.qindachang.bluetoothle.BluetoothConfig
import com.qindachang.bluetoothle.BluetoothLe

class LibraApplication: Application() {

    companion object {
        var instance: LibraApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        val config = BluetoothConfig.Builder()
            .enableQueueInterval(true)
            .setQueueIntervalTime(BluetoothConfig.AUTO) //设置队列间隔时间为自动
            .build()
        BluetoothLe.getDefault().init(this, config)
    }
}