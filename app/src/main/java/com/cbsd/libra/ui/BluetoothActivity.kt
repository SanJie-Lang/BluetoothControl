package com.cbsd.libra.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cbsd.libra.R
import com.cbsd.libra.adapter.BluetoothDataAdapter
import com.cbsd.libra.ble.BleScanHelper
import com.cbsd.libra.click
import com.cbsd.libra.ui.base.BaseActivity
import com.cbsd.libra.utils.LogUtils
import com.tbruyelle.rxpermissions3.RxPermissions
import kotlinx.android.synthetic.main.activity_bluetooth.*


class BluetoothActivity : BaseActivity() {

    companion object {
        private const val REQUEST_CODE: Int = 0x01
    }

    private var bluetoothDataAdapter: BluetoothDataAdapter? = null
    private val bluetoothList = arrayListOf<BluetoothDevice>()

    //蓝牙适配器
    private lateinit var mBluetoothAdapter: BluetoothAdapter

    //蓝牙辅助扫描
    private lateinit var mBleScanHelper: BleScanHelper

    //扫描时间
    private val mScanTime = 5000

    override fun layoutId(): Int = R.layout.activity_bluetooth

    override fun title(): String = "连接蓝牙"

    override fun initView() {
        bluetoothRv.layoutManager = LinearLayoutManager(this)
        bluetoothDataAdapter = BluetoothDataAdapter(this, bluetoothList)
        bluetoothDataAdapter!!.addOnItemClickListener { device, _ ->
            mBleScanHelper.stopScanBle()
            val bundle = Bundle()
            bundle.putParcelable("Bluetooth", device)
            result(bundle)
        }
        bluetoothRv.adapter = bluetoothDataAdapter
        bluetoothRescanBtn.click {
            bluetoothList.clear()
            bluetoothDataAdapter?.notifyDataSetChanged()
            scanBluetooth()
        }
    }

    override fun initData() {
        initBluetooth()
    }

    private fun scanBluetooth() {
        when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            true -> {
                RxPermissions(this).request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe {
                        log("蓝牙权限：$it")
                        when (it) {
                            false -> {
                                toast("为了能正常使用，请允许权限")
                            }
                            true -> {
                                mBleScanHelper.startScanBle(mScanTime)
                            }
                        }
                    }
            }
            else -> mBleScanHelper.startScanBle(mScanTime)
        }
        bluetoothRescanBtn.text = "扫描中"
        bluetoothRescanBtn.isEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mBleScanHelper.onDestroy()
    }

    //检查蓝牙是否支持及打开
    private fun initBluetooth() {
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = manager.adapter
        //判断蓝牙是否开启，如果关闭则请求打开蓝牙
        if (!mBluetoothAdapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_CODE)
        }
        mBleScanHelper = BleScanHelper(this)
        mBleScanHelper.setOnScanListener(object : BleScanHelper.OnScanListener {

            override fun onNext(device: BluetoothDevice) {
                if(!bluetoothList.contains(device)) {
                    bluetoothList.add(device)
                    bluetoothDataAdapter?.notifyDataSetChanged()
                }
            }

            override fun onFinish() {
                LogUtils.d("蓝牙扫描结束")
                bluetoothRescanBtn.text = "重新扫描"
                bluetoothRescanBtn.isEnabled = true
            }
        })
        scanBluetooth()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(resultCode == Activity.RESULT_OK){
            true -> {
                when(requestCode){
                    REQUEST_CODE -> {
                        RxPermissions(this).request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                            .subscribe {
                                log("蓝牙权限：$it")
                                when (it) {
                                    false -> {
                                        toast("为了能正常使用，请允许权限")
                                    }
                                    true -> {
                                        mBleScanHelper.startScanBle(mScanTime)
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

}