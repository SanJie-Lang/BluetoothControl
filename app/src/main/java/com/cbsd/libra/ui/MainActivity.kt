package com.cbsd.libra.ui

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelUuid
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cbsd.libra.*
import com.cbsd.libra.adapter.PopupAdapter
import com.cbsd.libra.adapter.VideoAdapter
import com.cbsd.libra.ble.BleConnectionHelper
import com.cbsd.libra.ble.UUID
import com.cbsd.libra.ble.mGattServiceList
import com.cbsd.libra.common.ACacheConfig
import com.cbsd.libra.listener.TSeekBarChangeListener
import com.cbsd.libra.model.BleData
import com.cbsd.libra.model.Color
import com.cbsd.libra.model.ViewPosition
import com.cbsd.libra.utils.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_popup.view.*
import java.util.concurrent.TimeUnit

/**
 * 默认为服务端，可切换客户端
 */
class MainActivity : AppCompatActivity() {

    private var mContext: Context? = null

    private val videos = intArrayOf(
        R.raw.video_1,
        R.raw.video_2,
        R.raw.video_3,
        R.raw.video_4,
        R.raw.video_5
    )
    // 红黄蓝绿黑白
    private val colors = arrayListOf(
        Color("红色", R.color.red, R.color.white), Color("黄色", R.color.orange, R.color.white),
        Color("蓝色", R.color.dodgerblue, R.color.white), Color("黑色", R.color.black, R.color.white), Color("白色", R.color.white, R.color.color_343434)
    )
    private var maxWidth = 0
    private var maxHeight = 0

    private var viewPosition: ViewPosition? = null

    //控制区域是否显示
    private var controlAreaIsShowing = true

    /*
    蓝牙开始
     */
    //广播时间(设置为0则持续广播)
    private val mTime = 0

    //蓝牙管理类
    private lateinit var mBluetoothManager: BluetoothManager

    //蓝牙适配器
    private lateinit var mBluetoothAdapter: BluetoothAdapter

    //GattService
    private lateinit var mGattService: BluetoothGattService

    //GattCharacteristic
    private lateinit var mGattCharacteristic: BluetoothGattCharacteristic

    //只读的GattCharacteristic
    private lateinit var mGattReadCharacteristic: BluetoothGattCharacteristic

    //GattDescriptor
    private lateinit var mGattDescriptor: BluetoothGattDescriptor

    //BLE广播操作类
    private lateinit var mBluetoothAdvertiser: BluetoothLeAdvertiser

    //蓝牙广播回调
    private val mAdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            LogUtils.d("蓝牙启动服务成功")
        }

        override fun onStartFailure(errorCode: Int) {
            LogUtils.d("蓝牙启动服务失败:$errorCode")
        }
    }

    //广播设置
    private lateinit var mAdvertiseSettings: AdvertiseSettings

    //广播数据（广播启动成功就发送）
    private lateinit var mAdvertiseData: AdvertiseData

    //扫描响应数据(可选，当客户端扫描时才发送)
    private lateinit var mScanResponseData: AdvertiseData

    //GattServer回调
    private val mBluetoothGattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            LogUtils.d("蓝牙连接状态：$status")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            LogUtils.d("蓝牙连接成功：${device?.address}")
                            ToastUtils.show("${device?.name}与你连接成功")
                        }
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            LogUtils.d("蓝牙连接断开：${device?.address}")
                        }

                    }
                }
                else -> {
                    LogUtils.d("蓝牙连接失败：${device?.address}")
                }
            }
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
            //添加本地服务回调
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            //特征值读取回调,相应客户端
            mBluetoothGattServer.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, characteristic?.value
            )
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            //特征值写入回调，刷新
            characteristic!!.value = value
            //相应客户端
            mBluetoothGattServer.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, value
            )
            LogUtils.d("接收到消息：${String(value!!)}")
            val data = BleData.instance(String(value))
            runOnUiThread {
                LogUtils.d("data.a：${data.a}")
                when (data.a) {
                    BleAction.DRAG -> {
                        val position = data.b
                        if (position.contains("#")) {
                            val positionData = position.split("#")

                            val lp = mainVideoView.layoutParams as FrameLayout.LayoutParams
                            lp.leftMargin = positionData[0].toInt()
                            lp.topMargin = positionData[1].toInt()
                            lp.rightMargin = positionData[2].toInt()
                            lp.bottomMargin = positionData[3].toInt()
                            lp.width = positionData[4].toInt()
                            lp.height = positionData[5].toInt()

                            mainWidthSeekBar.progress =
                                (lp.width.toDouble() / maxWidth * 100).toInt()
                            mainHeightSeekBar.progress =
                                (lp.height.toDouble() / maxHeight * 100).toInt()
                            mainVideoView.layoutParams = lp
                        }
                    }
                    BleAction.SWITCH_VIDEO -> {
                        play(data.b.toInt())
                    }
                    BleAction.MOVE_LEFT -> {
                        mainVideoView.toTheLeft(2)
                    }
                    BleAction.MOVE_TOP -> {
                        mainVideoView.toTheTop(2)
                    }
                    BleAction.MOVE_RIGHT -> {
                        mainVideoView.toTheRight(2)
                    }
                    BleAction.MOVE_BOTTOM -> {
                        mainVideoView.toTheBottom(2)
                    }
                    BleAction.CHANGE_WIDTH -> {
                        mainWidthSeekBar.progress = data.b.toInt()
                    }
                    BleAction.CHANGE_HEIGHT -> {
                        mainHeightSeekBar.progress = data.b.toInt()
                    }
                    BleAction.ZOOM -> {

                    }
                    BleAction.SWITCH_THEME -> {
                        setTheme(colors[data.b.toInt()])
                    }
                }
            }
        }

        override fun onDescriptorReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor?
        ) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
            //描述读取回调
            mBluetoothGattServer.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, descriptor?.value
            )
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onDescriptorWriteRequest(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            //刷新描述值
            descriptor?.value = value
            // 响应客户端
            mBluetoothGattServer.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, value
            )
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            LogUtils.d(if (status == BluetoothGatt.GATT_SUCCESS) "蓝牙通知发送成功" else "蓝牙通知发送失败:$status")
        }
    }

    //GattServer
    private lateinit var mBluetoothGattServer: BluetoothGattServer

    //连接
    private var bleConnectionHelper: BleConnectionHelper? = null
    private lateinit var connectCharacteristic: BluetoothGattCharacteristic
    private val bleConnectionListener = object : BleConnectionHelper.BleConnectionListener {
        override fun onConnectionSuccess() {
            LogUtils.d("连接成功")
            ToastUtils.show("连接成功")
        }

        override fun onConnectionFail() {
            LogUtils.d("连接失败")
            ToastUtils.show("连接失败")
        }

        override fun disConnection() {
            LogUtils.d("连接断开")
            ToastUtils.show("连接断开")
        }

        override fun discoveredServices() {
            //发现服务后查找写入服务
            Observable.fromIterable(mGattServiceList)
                .subscribe { it ->
                    when (it.uuid.toString()) {
                        "00001801-0000-1000-8000-00805f9b34fb" -> {
                        }
                        "00001800-0000-1000-8000-00805f9b34fb" -> {
                        }
                        else -> {
                            it.characteristics?.let { characteristics ->
                                for (characteristic in characteristics) {
                                    //获取特征属性
                                    val propertiesStr = getProperties(characteristic.properties)
                                    val propertiesArray = propertiesStr.split(",")
                                    Observable.fromIterable(propertiesArray)
                                        .subscribe {
                                            when (it) {
                                                "READ" -> {

                                                }
                                                "WRITE NO RESPONSE",
                                                "WRITE" -> {
                                                    connectCharacteristic = characteristic
                                                }
                                                "NOTIFY" -> {
                                                    //设置通知
                                                    bleConnectionHelper?.setCharacteristicNotification(
                                                        characteristic
                                                    )
                                                }
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
        }

        override fun readCharacteristic(data: String) {
        }

        override fun writeCharacteristic(data: String) {
        }

        override fun readDescriptor(data: String) {
        }

        override fun writeDescriptor(data: String) {
        }

        override fun characteristicChange(data: String) {
        }
    }
    /*
    蓝牙结束
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this
        maxWidth = DisplayUtils.getScreenWidth(this)
        maxHeight = DisplayUtils.getScreenHeight(this)
        initBluetooth()//初始化蓝牙
        if (ACache[this].getAsObject(ACacheConfig.POSITION) != null)
            viewPosition = ACache[this].getAsObject(ACacheConfig.POSITION) as ViewPosition
        if (viewPosition != null) {
            useCache()
            LogUtils.d("viewPosition：" + GsonUtils.toJson(viewPosition))
        } else {
            viewPosition = ViewPosition(0, 0, 0, 0, 0, 0)
            mainWidthSeekBar.progress =
                (DisplayUtils.dp2px(this, 214F).toDouble() / maxWidth * 100).toInt()
            mainHeightSeekBar.progress =
                (DisplayUtils.dp2px(this, 120F).toDouble() / maxHeight * 100).toInt()
        }
        initView()
        click()
    }


    override fun onRestart() {
        super.onRestart()
        LogUtils.d("Life cycle onRestart")
    }

    override fun onPause() {
        super.onPause()
        mainVideoView.pause()
        LogUtils.d("Life cycle onPause")
    }

    override fun onResume() {
        super.onResume()
        mainVideoView.resume()
        mainVideoView.start()
        LogUtils.d("Life cycle onResume")
    }

    private fun initView() {
        mainRv.layoutManager = LinearLayoutManager(this)
        val videoAdapter = VideoAdapter(this)
        videoAdapter.addOnItemClickListener {
            play(it)
            sendMessage(BleData(BleAction.SWITCH_VIDEO, it.toString()).toJson())
        }
        mainRv.adapter = videoAdapter

        val mediaPlayer = arrayOfNulls<MediaPlayer>(1)
        mainVideoView.setOnInfoListener { mp, _, _ ->
            mediaPlayer[0] = mp
            mp.isLooping = true
            false
        }
        play(0)
        startTimingGoneControlArea()

    }

    private fun play(index: Int) {
        ACache[this].put(ACacheConfig.VIDEO_INDEX, index.toString())
        mainVideoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + videos[index]))
        mainVideoView.start()
    }

    private fun releaseVideo() {
        mainVideoView.stopPlayback()
    }

    private fun click() {
        mainVideoContainer.setOnClickListener {
            controlAreaIsShowing = !controlAreaIsShowing
            showControlArea()
        }

        mainLeftBtn.click {
            mainVideoView.toTheLeft(2)
            //需加入客户端 服务端判断
            sendMessage(BleData(BleAction.MOVE_LEFT, "2").toJson())
        }
        var leftDisposable: Disposable? = null
        mainLeftBtn.longClick {
            leftDisposable = DisposableUtils.createInterval(100, TimeUnit.MILLISECONDS) {
                mainVideoView.toTheLeft(2)
                sendMessage(BleData(BleAction.MOVE_LEFT, "2").toJson())
            }
        }
        mainLeftBtn.touchUp {
            if (leftDisposable != null && !leftDisposable!!.isDisposed) {
                leftDisposable!!.dispose()
                sendMessage(BleData(BleAction.LONG_STOP, "-1").toJson())
            }
        }

        mainUpBtn.click {
            mainVideoView.toTheTop(2)
            sendMessage(BleData(BleAction.MOVE_TOP, "2").toJson())
        }
        var upDisposable: Disposable? = null
        mainUpBtn.longClick {
            upDisposable = DisposableUtils.createInterval(100, TimeUnit.MILLISECONDS) {
                mainVideoView.toTheTop(2)
                sendMessage(BleData(BleAction.MOVE_TOP, "2").toJson())
            }
        }
        mainUpBtn.touchUp {
            if (upDisposable != null && !upDisposable!!.isDisposed) {
                upDisposable!!.dispose()
                sendMessage(BleData(BleAction.LONG_STOP, "-1").toJson())
            }
        }

        mainRightBtn.click {
            mainVideoView.toTheRight(2)
            sendMessage(BleData(BleAction.MOVE_RIGHT, "2").toJson())
        }
        var rightDisposable: Disposable? = null
        mainRightBtn.longClick {
            rightDisposable = DisposableUtils.createInterval(100, TimeUnit.MILLISECONDS) {
                mainVideoView.toTheRight(2)
                sendMessage(BleData(BleAction.MOVE_RIGHT, "2").toJson())
            }
        }
        mainRightBtn.touchUp {
            if (rightDisposable != null && !rightDisposable!!.isDisposed) {
                rightDisposable!!.dispose()
                sendMessage(BleData(BleAction.LONG_STOP, "2").toJson())
            }
        }

        mainBottomBtn.click {
            mainVideoView.toTheBottom(2)
            sendMessage(BleData(BleAction.MOVE_BOTTOM, "2").toJson())
        }
        var bottomDisposable: Disposable? = null
        mainBottomBtn.longClick {
            bottomDisposable = DisposableUtils.createInterval(100, TimeUnit.MILLISECONDS) {
                mainVideoView.toTheBottom(2)
                sendMessage(BleData(BleAction.MOVE_BOTTOM, "2").toJson())
            }
        }
        mainBottomBtn.touchUp {
            if (bottomDisposable != null && !bottomDisposable!!.isDisposed) {
                bottomDisposable!!.dispose()
                sendMessage(BleData(BleAction.LONG_STOP, "2").toJson())
            }
        }

        mainWidthLessBtn.click {
            var progress = mainWidthSeekBar.progress
            if (progress >= 0) {
                progress--
                mainWidthSeekBar.progress = progress
            }
        }

        var mainWidthLessDisposable: Disposable? = null
        mainWidthLessBtn.longClick {
            mainWidthLessDisposable = DisposableUtils.createInterval(100, TimeUnit.MILLISECONDS) {
                var progress = mainWidthSeekBar.progress
                if (progress >= 0) {
                    progress--
                    mainWidthSeekBar.progress = progress
                } else
                    mainWidthLessDisposable!!.dispose()
            }
        }
        mainWidthLessBtn.touchUp {
            if (mainWidthLessDisposable != null && !mainWidthLessDisposable!!.isDisposed) {
                mainWidthLessDisposable!!.dispose()
            }
        }


        mainWidthAddBtn.click {
            var progress = mainWidthSeekBar.progress
            if (progress <= 100) {
                progress++
                mainWidthSeekBar.progress = progress
            }
        }
        var mainWidthAddDisposable: Disposable? = null
        mainWidthAddBtn.longClick {
            mainWidthAddDisposable = DisposableUtils.createInterval(100, TimeUnit.MILLISECONDS) {
                var progress = mainWidthSeekBar.progress
                if (progress <= 100) {
                    progress++
                    mainWidthSeekBar.progress = progress
                } else
                    mainWidthAddDisposable!!.dispose()
            }
        }
        mainWidthAddBtn.touchUp {
            if (mainWidthAddDisposable != null && !mainWidthAddDisposable!!.isDisposed) {
                mainWidthAddDisposable!!.dispose()
            }
        }

        mainHeightLessBtn.click {
            var progress = mainHeightSeekBar.progress
            if (progress >= 0) {
                progress--
                sendMessage(BleData(BleAction.CHANGE_HEIGHT, progress.toString()).toJson())
                mainHeightSeekBar.progress = progress
            }
        }
        var mainHeightLessDisposable: Disposable? = null
        mainHeightLessBtn.longClick {
            mainHeightLessDisposable = DisposableUtils.createInterval(100, TimeUnit.MILLISECONDS) {
                var progress = mainHeightSeekBar.progress
                if (progress >= 0) {
                    progress--
                    mainHeightSeekBar.progress = progress
                } else
                    mainHeightLessDisposable!!.dispose()
            }
        }
        mainHeightLessBtn.touchUp {
            if (mainHeightLessDisposable != null && !mainHeightLessDisposable!!.isDisposed) {
                mainHeightLessDisposable!!.dispose()
            }
        }

        mainHeightAddBtn.click {
            var progress = mainHeightSeekBar.progress
            if (progress <= 100) {
                progress++
                sendMessage(BleData(BleAction.CHANGE_HEIGHT, progress.toString()).toJson())
                mainHeightSeekBar.progress = progress
            }
        }
        var mainHeightAddDisposable: Disposable? = null
        mainHeightAddBtn.longClick {
            mainHeightAddDisposable = DisposableUtils.createInterval(100, TimeUnit.MILLISECONDS) {
                var progress = mainHeightSeekBar.progress
                if (progress <= 100) {
                    progress++
                    mainHeightSeekBar.progress = progress
                } else
                    mainHeightAddDisposable!!.dispose()
            }
        }
        mainHeightAddBtn.touchUp {
            if (mainHeightAddDisposable != null && !mainHeightAddDisposable!!.isDisposed) {
                mainHeightAddDisposable!!.dispose()
            }
        }

        mainWidthSeekBar.setOnSeekBarChangeListener(object : TSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
                mainVideoView.width = maxWidth / 100 * progress
//                sendMessage(BleData(BleAction.CHANGE_WIDTH, progress.toString()).toJson())
            }
        })

        mainHeightSeekBar.setOnSeekBarChangeListener(object : TSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
                mainVideoView.height = maxHeight / 100 * progress
//                sendMessage(BleData(BleAction.CHANGE_HEIGHT, progress.toString()).toJson())
            }
        })

        mainVideoView.setOnViewPositionChangedListener {
            ACache[this].put(ACacheConfig.POSITION, it)
            val sb = StringBuilder()
            sb.append(it.left)
            sb.append("#")
            sb.append(it.top)
            sb.append("#")
            sb.append(it.right)
            sb.append("#")
            sb.append(it.bottom)
            sb.append("#")
            sb.append(it.width)
            sb.append("#")
            sb.append(it.height)
            sendMessage(BleData(BleAction.DRAG, sb.toString()).toJson())
        }

        mainVideoView.setOnScaleChangedListener { width, height ->
            mainWidthSeekBar.progress = (width.toDouble() / maxWidth * 100).toInt()
            mainHeightSeekBar.progress = (height.toDouble() / maxHeight * 100).toInt()
            sendMessage(BleData(BleAction.ZOOM, width.toString()).toJson())
        }

        mainRemoteControlBtn.click {
            if (isLogin)
                startActivityForResult(Intent(this, BluetoothActivity::class.java), 1001)
            else startActivityForResult(Intent(this, LoginActivity::class.java), 1002)
        }

        mainThemeBtn.click {
            showPopup()
        }
    }

    private fun showPopup(){
        val popupWindow = PopupWindow()
        val popupView = layoutInflater.inflate(R.layout.include_popup, null, false)
        popupView.popupRv.layoutManager = LinearLayoutManager(this)
        val popupAdapter = PopupAdapter(this, colors)
        popupAdapter.setOnColorSelectedListener { color, position ->
            setTheme(color)
            sendMessage(BleData(BleAction.SWITCH_THEME, position.toString()).toJson())
            popupWindow.dismiss()
        }
        popupView.popupRv.adapter = popupAdapter
        popupWindow.contentView = popupView
        popupWindow.isOutsideTouchable = true
        popupWindow.width = DisplayUtils.dp2px(this, 100F)
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.showAsDropDown(mainThemeBtn)
    }

    private fun setTheme(color: Color){
        mainFrame.setBackgroundColor(ContextCompat.getColor(this, color.colorRes))
        mainThemeBtn.setTextColor(ContextCompat.getColor(this, color.textColorRes))
        mainRemoteControlBtn.setTextColor(ContextCompat.getColor(this, color.textColorRes))
        mainWidthTitleTv.setTextColor(ContextCompat.getColor(this, color.textColorRes))
        mainHeightTitleTv.setTextColor(ContextCompat.getColor(this, color.textColorRes))
    }

    private var isLogin = false

    private fun useCache() {
        //读取缓存video属性
        if (viewPosition!!.width == 0)
            viewPosition!!.width = DisplayUtils.dp2px(this, 214F)
        if (viewPosition!!.height == 0)
            viewPosition!!.height = DisplayUtils.dp2px(this, 120F)
        mainWidthSeekBar.progress = (viewPosition!!.width.toDouble() / maxWidth * 100).toInt()
        mainHeightSeekBar.progress = (viewPosition!!.height.toDouble() / maxHeight * 100).toInt()
        val lp = mainVideoView.layoutParams as FrameLayout.LayoutParams
        lp.width = viewPosition!!.width
        lp.height = viewPosition!!.height
        lp.leftMargin = viewPosition!!.left
        lp.topMargin = viewPosition!!.top
        lp.rightMargin = viewPosition!!.right
        lp.bottomMargin = viewPosition!!.bottom
        mainVideoView.layoutParams = lp
    }

    private fun showControlArea() {
        mainRv.visibility = if (controlAreaIsShowing) View.VISIBLE else View.GONE
        mainLpControlContainer.visibility = if (controlAreaIsShowing) View.VISIBLE else View.GONE
        if (timingDisposable != null && !timingDisposable!!.isDisposed) {
            timingDisposable!!.dispose()
            timingDisposable = null
        }
        if (controlAreaIsShowing)
            startTimingGoneControlArea()
    }

    private var timingDisposable: Disposable? = null
    private fun startTimingGoneControlArea() {
        timingDisposable = Observable.timer(7, TimeUnit.SECONDS)
            .compose(TransformUtils.defaultSchedulers())
            .subscribe({
                controlAreaIsShowing = false
                showControlArea()
            }, {
                LogUtils.d("startTimingGoneControlArea：${it.message}")
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode == Activity.RESULT_OK) {
            true -> {
                when (requestCode) {
                    1001 -> {
                        //选择蓝牙返回，进行连接
                        val device = data?.getParcelableExtra<BluetoothDevice>("Bluetooth")
                        LogUtils.d("选择蓝牙:${device?.address}")
                        stopAdvertising()
                        initBluetoothConnect(device?.address!!)
                    }
                    1002 -> {
                        isLogin = true
                        startActivityForResult(Intent(this, BluetoothActivity::class.java), 1001)
                    }
                    10086 -> {
                        startAdvertising()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initBluetoothConnect(macAddress: String) {
        if (bleConnectionHelper == null) {
            bleConnectionHelper = BleConnectionHelper(this)
            bleConnectionHelper?.setBleConnectionListener(bleConnectionListener)
        }
        if (bleConnectionHelper?.isConnected!!) {
            bleConnectionHelper?.disConnection()
        }
        //连接
        bleConnectionHelper?.connection(macAddress)
    }

    private fun sendMessage(message: String?) {
        bleConnectionHelper?.writeCharacteristic(connectCharacteristic, message!!.toByteArray())
    }

    private fun initBluetooth() {
        mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter
        //蓝牙初始化成功，自动发送广播使可连接
        startAdvertising()
    }

    /**
     * 发送广播
     */
    private fun startAdvertising() {
        if (!mBluetoothAdapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, 10086)
            return
        }
        //初始化广播设置
        mAdvertiseSettings = AdvertiseSettings.Builder()
            //设置广播模式，以控制广播的功率和延迟。 ADVERTISE_MODE_LOW_LATENCY为高功率，低延迟
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            //设置蓝牙广播发射功率级别
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            //广播时限。最多180000毫秒。值为0将禁用时间限制。（不设置则为无限广播时长）
            .setTimeout(mTime)
            //设置广告类型是可连接还是不可连接。
            .setConnectable(true)
            .build()
        //设置广播报文
        mAdvertiseData = AdvertiseData.Builder()
            //设置广播包中是否包含设备名称。
            .setIncludeDeviceName(true)
            //设置广播包中是否包含发射功率
            .setIncludeTxPowerLevel(true)
            //设置UUID
            .addServiceUuid(ParcelUuid(UUID.SERVICE_UUID))
            .addServiceUuid(ParcelUuid(UUID.SERVICE_UUID_2))
            .build()
        //设置广播扫描响应报文(可选)
        mScanResponseData = AdvertiseData.Builder()
            //自定义服务数据，将其转化为字节数组传入
            .addServiceData(ParcelUuid(UUID.SERVICE_UUID_2), byteArrayOf(2, 3, 4))
            //设备厂商自定义数据，将其转化为字节数组传入
            .addManufacturerData(0x06, byteArrayOf(1, 2, 3))
            .build()
        mBluetoothAdvertiser = mBluetoothAdapter.bluetoothLeAdvertiser
        if (mBluetoothAdvertiser != null && mBluetoothAdapter.isEnabled) {
            LogUtils.d("蓝牙支持BLE广播,开始发送广播")
            //开始广播
            mBluetoothAdvertiser.startAdvertising(
                mAdvertiseSettings, mAdvertiseData,
                mScanResponseData, mAdvertiseCallback
            )
        } else {
            ToastUtils.show("您的设备不支持BLE广播")
            LogUtils.d("设备蓝牙不支持支持BLE广播")
        }
        addGattServer()
    }

    /**
     * 停止发送广播
     * 当选择控制其他设备时调用此方法
     */
    private fun stopAdvertising() {
        mBluetoothAdvertiser?.let {
            it.stopAdvertising(mAdvertiseCallback)
        }
    }

    /**
     * 添加Gatt 服务和特征
     * 广播是广播，只有添加Gatt服务和特征后，连接才有服务和特征用于数据交换
     */
    private fun addGattServer() {
        /*
        初始化服务
        创建服务，用初始化服务UUID和服务类型
         */
        mGattService =
            BluetoothGattService(UUID.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        /*
        初始化特征
         */
        mGattCharacteristic = BluetoothGattCharacteristic(
            UUID.CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                    BluetoothGattCharacteristic.PROPERTY_READ,
            (BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ)
        )
        mGattCharacteristic.value = byteArrayOf(17, 2, 85)
        //设置只读
        mGattReadCharacteristic = BluetoothGattCharacteristic(
            UUID.CHARACTERISTIC_UUID_2,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        //初始化描述
        mGattDescriptor = BluetoothGattDescriptor(
            UUID.DESCRIPTOR_UUID,
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        mGattDescriptor.value = byteArrayOf(12, 33, 54, 45)
        //Service添加特征值
        mGattService.addCharacteristic(mGattCharacteristic)
        mGattService.addCharacteristic(mGattReadCharacteristic)
        //特征值添加描述
        mGattCharacteristic.addDescriptor(mGattDescriptor)

        //添加服务
        if (mBluetoothManager != null)
            mBluetoothGattServer =
                mBluetoothManager.openGattServer(this, mBluetoothGattServerCallback)
        mBluetoothGattServer.addService(mGattService)
    }

    /**
     * 获取具体属性
     */
    private fun getProperties(properties: Int): String {
        val buffer = StringBuffer()
        for (i in 1..8) {
            when (i) {
                1 -> if (properties and BluetoothGattCharacteristic.PROPERTY_BROADCAST != 0)
                    buffer.append("BROADCAST,")
                2 -> if (properties and BluetoothGattCharacteristic.PROPERTY_READ != 0)
                    buffer.append("READ,")
                3 -> if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0)
                    buffer.append("WRITE NO RESPONSE,")
                4 -> if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0)
                    buffer.append("WRITE,")
                5 -> if (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)
                    buffer.append("NOTIFY,")
                6 -> if (properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0)
                    buffer.append("INDICATE,")
                7 -> if (properties and BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE != 0)
                    buffer.append("SIGNED WRITE,")
                8 -> if (properties and BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS != 0)
                    buffer.append("EXTENDED PROPS,")
            }
        }
        val str = buffer.toString()
        if (str.isNotEmpty())
        //减最后的逗号
            return str.substring(0, str.length - 1)
        else
            return ""
    }

}