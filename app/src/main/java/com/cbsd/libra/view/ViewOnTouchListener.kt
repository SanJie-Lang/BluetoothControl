package com.cbsd.libra.view

import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

class ViewOnTouchListener(
    private val mPushView: View,
    var maxX: Int,
    var maxY: Int
) : View.OnTouchListener {
    var pushPoint: Point? = null
    var lastImgLeft = 0
    var lastImgTop = 0
    var lastImgRight = 0
    var lastImgBottom = 0
    var viewLP: FrameLayout.LayoutParams? = null
    var pushBtnLP: FrameLayout.LayoutParams? = null
    var lastPushBtnLeft = 0
    var lastPushBtnTop = 0
    var moveX = 0f
    var moveY = 0f
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mPushView.visibility = View.VISIBLE
                if (null == viewLP) {
                    viewLP = view.layoutParams as FrameLayout.LayoutParams
                }
                if (null == pushBtnLP) {
                    pushBtnLP = mPushView.layoutParams as FrameLayout.LayoutParams
                }
                pushPoint = getRawPoint(event)
                lastImgLeft = viewLP!!.leftMargin
                lastImgTop = viewLP!!.topMargin
                lastImgRight = viewLP!!.rightMargin
                lastImgBottom = viewLP!!.bottomMargin
                lastPushBtnLeft = pushBtnLP!!.leftMargin
                lastPushBtnTop = pushBtnLP!!.topMargin
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val newPoint = getRawPoint(event)
                moveX = newPoint.x - pushPoint!!.x
                moveY = newPoint.y - pushPoint!!.y
                if (lastImgLeft + moveX <= 0 - viewLP!!.width / 2) {
                    return true
                } else if (lastImgLeft + viewLP!!.width + moveX > maxX + viewLP!!.width / 2) {
                    return true
                }
                if (lastImgTop + moveY <= 0 - viewLP!!.height / 2) {
                    return true
                } else if (lastImgTop + viewLP!!.height + moveY > maxY + viewLP!!.height / 2) {
                    return true
                }
                viewLP!!.leftMargin = (lastImgLeft + moveX).toInt()
                viewLP!!.topMargin = (lastImgTop + moveY).toInt()
                view.layoutParams = viewLP
                pushBtnLP!!.leftMargin = (lastPushBtnLeft + moveX).toInt()
                pushBtnLP!!.topMargin = (lastPushBtnTop + moveY).toInt()
                mPushView.layoutParams = pushBtnLP
            }
        }
        return false
    }

    private fun getRawPoint(event: MotionEvent): Point {
        return Point(event.rawX, event.rawY)
    }
}