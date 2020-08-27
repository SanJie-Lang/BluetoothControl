package com.cbsd.libra.ui.base

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cbsd.libra.R
import com.cbsd.libra.utils.LogUtils
import com.cbsd.libra.utils.ToastUtils
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.toolbar.*

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
        StatusBarUtil.setLightMode(this)
        initToolbar()
        initView()
        initData()
    }

    abstract fun layoutId(): Int
    abstract fun title(): String
    abstract fun initView()
    abstract fun initData()

    private fun initToolbar() {
        if (toolbar != null) {
            toolbarTitleTv.text = title()
            toolbar.title = ""
            setSupportActionBar(toolbar)
            toolbar.setNavigationIcon(R.mipmap.icon_black_back)
            toolbar.setNavigationOnClickListener {
                finish()
            }
        }
    }

    /**
     * 跳转到指定Activity
     * @param cls Activity
     */
    fun jumpTo(cls: Class<*>) {
        startActivity(Intent(this, cls))
    }

    /**
     * 传值跳转到指定Activity
     * @param cls Activity
     * @param bundle 传值bundle
     */
    fun jumpTo(cls: Class<*>, bundle: Bundle) {
        val intent = Intent(this, cls)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    /**
     * 跳转到指定Activity并返回值
     * @param cls Activity
     * @param requestCode 返回code
     */
    fun jumpForResult(cls: Class<*>, requestCode: Int) {
        startActivityForResult(Intent(this, cls), requestCode)
    }

    /**
     * 传值跳转到指定Activity并返回值
     * @param cls Activity
     * @param bundle 传值bundle
     * @param requestCode 返回code
     */
    fun jumpForResult(cls: Class<*>, bundle: Bundle, requestCode: Int) {
        val intent = Intent(this, cls)
        intent.putExtras(bundle)
        startActivityForResult(intent, requestCode)
    }

    fun result(bundle: Bundle){
        val intent = Intent()
        intent.putExtras(bundle)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    /**
     * 打印log日志
     */
    fun log(message: String) {
        LogUtils.d(message)
    }

    /**
     * 显示toast
     */
    fun toast(message: String) {
        ToastUtils.show(message)
    }

    /**
     * 获取屏幕宽度
     */
    fun screenWidth(): Int {
        val display = windowManager.defaultDisplay
        return if (display != null) {
            val point = Point()
            display.getSize(point)
            point.x
        } else
            0
    }

    /**
     * 获取屏幕高度
     */
    fun screenHeight(): Int {
        val display = windowManager.defaultDisplay
        return if (display != null) {
            val point = Point()
            display.getSize(point)
            point.y
        } else
            0
    }

    /**
     * 获取状态栏高度
     */
    fun statusBarHeight(): Int {
        val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0)
            resources.getDimensionPixelOffset(resId)
        else
//            resources.getDimensionPixelOffset(R.dimen.dimen_25dp)
            0
    }

    /**
     * 获取app外部版本号
     */
    fun versionName(): String {
        var packageInfo: PackageInfo? = null
        return try {
            packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo!!.versionName
        }catch (e: PackageManager.NameNotFoundException){
            ""
        }
    }

    /**
     * 获取app内部版本号
     */
    fun versionCode(): Int {
        var packageInfo: PackageInfo? = null
        return try {
            packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo!!.versionCode
        }catch (e: PackageManager.NameNotFoundException){
            0
        }
    }
}