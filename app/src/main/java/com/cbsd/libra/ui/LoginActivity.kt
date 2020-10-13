package com.cbsd.libra.ui

import android.os.Bundle
import com.cbsd.libra.R
import com.cbsd.libra.click
import com.cbsd.libra.common.ACacheConfig
import com.cbsd.libra.ui.base.BaseActivity
import com.cbsd.libra.utils.ACache
import com.cbsd.libra.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: BaseActivity() {

    override fun layoutId(): Int = R.layout.activity_login

    override fun title(): String? = null

    override fun initView() {
        loginBtn.click {
            val username = loginUsernameEt.text.toString().trim()
            val password = loginPasswordEt.text.toString().trim()
            if (username.isNotEmpty() && password.isNotEmpty()){
                //登陆成功
                ACache[this].put(ACacheConfig.USER, "success")
                ToastUtils.show("登录成功")
                val bundle = Bundle()
                bundle.putBoolean("isLogin", true)
                result(bundle)
            }else{
                ToastUtils.show("账号和密码错误")
            }
        }
    }

    override fun initData() {
    }
}