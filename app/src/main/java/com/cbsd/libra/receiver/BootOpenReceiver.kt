package com.cbsd.libra.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cbsd.libra.ui.MainActivity

class BootOpenReceiver: BroadcastReceiver() {

    private val action = "android.intent.action.BOOT_COMPLETED"

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action == action){
            true -> {
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context!!.startActivity(intent)
            }
        }
    }
}