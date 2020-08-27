package com.cbsd.libra.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cbsd.libra.R
import com.cbsd.libra.click
import com.cbsd.libra.listener.OnItemClickListener
import kotlinx.android.synthetic.main.item_bluetooth.view.*

class BluetoothDataAdapter(private val mContext: Context, private val bluetoothList: ArrayList<BluetoothDevice>):
    RecyclerView.Adapter<BluetoothDataAdapter.BluetoothHolder>(){

    private var onItemClickListener: OnItemClickListener<BluetoothDevice>? = null


    fun addOnItemClickListener(click: (device: BluetoothDevice, position: Int) -> Unit){
        if(onItemClickListener == null)
            onItemClickListener = object : OnItemClickListener<BluetoothDevice>{
                override fun onItemClick(t: BluetoothDevice, position: Int) {
                    click(t, position)
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothHolder {
        return BluetoothHolder(LayoutInflater.from(mContext).inflate(R.layout.item_bluetooth, parent, false))
    }

    override fun getItemCount(): Int = bluetoothList.size

    override fun onBindViewHolder(holder: BluetoothHolder, position: Int) {
        val device = bluetoothList[position]
        holder.itemView.itemBluetoothNameTv.text = when(device.name.isNullOrEmpty()){
            true -> "未命名"
            else -> device.name
        }
        holder.itemView.itemBluetoothMacTv.text = when(device.address.isNullOrEmpty()){
            true -> ""
            else -> when(device.bondState == 10){
                true -> "${device.address}\u3000（已配对）"
                else -> "${device.address}\u3000（未配对）"
            }
        }
        holder.itemView.itemBluetoothConnectBtn.click {
            onItemClickListener?.onItemClick(device, position)
        }
    }

    inner class BluetoothHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}