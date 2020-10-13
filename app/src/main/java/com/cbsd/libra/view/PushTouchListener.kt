package com.cbsd.libra.view

import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.cbsd.libra.R

internal class PushTouchListener(private val mView: View) :
    View.OnTouchListener {
    var pushPoint: Point? = null
    var lastImgWidth = 0
    var lastImgHeight = 0
    var lastImgLeft = 0
    var lastImgTop = 0
    var lastImgAngle = 0
    var lastComAngle = 0.0
    var pushImgWidth = 0
    var pushImgHeight = 0
    var lastPushBtnLeft = 0
    var lastPushBtnTop = 0
    private var mViewCenter: Point? = null
    private var pushBtnLP: FrameLayout.LayoutParams? = null
    private var imgLP: FrameLayout.LayoutParams? = null
    var lastX = -1f
    var lastY = -1f
    override fun onTouch(pushView: View, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                pushBtnLP = pushView.layoutParams as FrameLayout.LayoutParams
                imgLP = mView.layoutParams as FrameLayout.LayoutParams
                pushPoint = getPushPoint(pushBtnLP, event)
                lastImgWidth = imgLP!!.width
                lastImgHeight = imgLP!!.height
                lastImgLeft = imgLP!!.leftMargin
                lastImgTop = imgLP!!.topMargin
                lastImgAngle = mView.rotation.toInt()
                lastPushBtnLeft = pushBtnLP!!.leftMargin
                lastPushBtnTop = pushBtnLP!!.topMargin
                pushImgWidth = pushBtnLP!!.width
                pushImgHeight = pushBtnLP!!.height
                lastX = event.rawX
                lastY = event.rawY
                refreshImageCenter()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
            }
            MotionEvent.ACTION_UP -> {
            }
            MotionEvent.ACTION_POINTER_UP -> {
            }
            MotionEvent.ACTION_MOVE -> {
                val rawX = event.rawX
                val rawY = event.rawY
                if (lastX != -1f) {
                    if (Math.abs(rawX - lastX) < 5 && Math.abs(rawY - lastY) < 5) {
                        return false
                    }
                }
                lastX = rawX
                lastY = rawY
                val O = mViewCenter
                val A = pushPoint
                val B = getPushPoint(pushBtnLP, event)
                val dOA = getDistance(O, A)
                val dOB = getDistance(O, B)
                val f = dOB / dOA
                val newWidth = (lastImgWidth * f).toInt()
                val newHeight = (lastImgHeight * f).toInt()
                mView.setTag(R.id.single_finger_view_scale, f)
                imgLP!!.leftMargin = lastImgLeft - (newWidth - lastImgWidth) / 2
                imgLP!!.topMargin = lastImgTop - (newHeight - lastImgHeight) / 2
                imgLP!!.width = newWidth
                imgLP!!.height = newHeight
                mView.layoutParams = imgLP
                val fz = (A!!.x - O!!.x) * (B.x - O.x) + (A.y - O.y) * (B.y - O.y)
                val fm = dOA * dOB
                var comAngle = 180 * Math.acos(fz / fm.toDouble()) / PI
                if (java.lang.Double.isNaN(comAngle)) {
                    comAngle = if (lastComAngle < 90 || lastComAngle > 270) 0.0 else 180.toDouble()
                } else if ((B.y - O.y) * (A.x - O.x) < (A.y - O.y) * (B.x - O.x)) {
                    comAngle = 360 - comAngle
                }
                lastComAngle = comAngle
                var angle = (lastImgAngle + comAngle).toFloat()
                angle %= 360
                mView.rotation = angle
                val imageRB = Point(
                    (mView.left + mView.width).toFloat(),
                    (mView.top + mView.height).toFloat()
                )
                val anglePoint = getAnglePoint(O, imageRB, angle)
                val deleteAnglePoint = getAnglePoint1(O, imageRB, angle)
                pushBtnLP!!.leftMargin = (anglePoint.x - pushImgWidth / 2).toInt()
                pushBtnLP!!.topMargin = (anglePoint.y - pushImgHeight / 2).toInt()
                pushView.layoutParams = pushBtnLP
            }
        }
        return false
    }

    private fun refreshImageCenter() {
        val x = mView.left + mView.width / 2
        val y = mView.top + mView.height / 2
        mViewCenter = Point(x.toFloat(), y.toFloat())
    }

    private fun getPushPoint(lp: FrameLayout.LayoutParams?, event: MotionEvent): Point {
        return Point(
            (lp!!.leftMargin + event.x.toInt()).toFloat(), (lp.topMargin + event.y
                .toInt()).toFloat()
        )
    }

    private fun getDistance(a: Point?, b: Point?): Float {
        val v = (a!!.x - b!!.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
        return (Math.sqrt(v.toDouble()) * 100).toInt() / 100f
    }

    private fun getAnglePoint(O: Point?, A: Point, angle: Float): Point {
        val x: Int
        val y: Int
        val dOA = getDistance(O, A)
        val p1 = angle * PI / 180f
        val p2 = Math.acos((A.x - O!!.x) / dOA.toDouble())
        x = (O.x + dOA * Math.cos(p1 + p2)).toInt()
        val p3 = Math.acos((A.x - O.x) / dOA.toDouble())
        y = (O.y + dOA * Math.sin(p1 + p3)).toInt()
        return Point(x.toFloat(), y.toFloat())
    }

    private fun getAnglePoint1(O: Point?, A: Point, angle: Float): Point {
        val x: Int
        val y: Int
        val dOA = getDistance(O, A)
        val p1 = angle * PI / 180f
        val p2 = Math.acos((A.x - O!!.x) / dOA.toDouble())
        x = (O.x - dOA * Math.cos(p1 + p2)).toInt()
        val p3 = Math.acos((A.x - O.x) / dOA.toDouble())
        y = (O.y - dOA * Math.sin(p1 + p3)).toInt()
        return Point(x.toFloat(), y.toFloat())
    }

    companion object {
        private const val PI = 3.14159265359
    }
}