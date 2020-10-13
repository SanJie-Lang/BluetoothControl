package com.cbsd.libra.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import com.cbsd.libra.utils.LogUtils
import kotlin.math.sqrt

class DragLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    // 属性变量
    private var transX // 移动X
            = 0f
    private var transY // 移动Y
            = 0f
    private var scale = 1f // 伸缩比例
    private var mRotation // 旋转角度
            = 0f

    // 移动过程中临时变量
    private var actionX = 0f
    private var actionY = 0f
    private var spacing = 0f
    private var degree = 0f
    private var moveType // 0=未选择，1=拖动，2=缩放
            = 0

    private lateinit var onPositionChangedListener: OnPositionChangedListener

    fun addOnPositionChangedListener(changed:(translationX: Float, translationY: Float, scale: Float) -> Unit){
        onPositionChangedListener = object : OnPositionChangedListener{
            override fun onPositionChanged(translationX: Float, translationY: Float, scale: Float) {
                changed(translationX, translationY, scale)
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.onInterceptTouchEvent(ev)
    }

    fun toLeft() {
        translationX = 2.let { transX -= it; transX }
        onPositionChangedListener.onPositionChanged(translationX, translationY, scale)
    }

    fun toRight() {
        translationX = 2.let { transX += it; transX }
        onPositionChangedListener.onPositionChanged(translationX, translationY, scale)
    }

    fun toTop() {
        translationY = 2.let { transY -= it; transY }
        onPositionChangedListener.onPositionChanged(translationX, translationY, scale)
    }

    fun toBottom() {
        translationY = 2.let { transY += it; transY }
        onPositionChangedListener.onPositionChanged(translationX, translationY, scale)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                moveType = 1
                actionX = event.rawX
                actionY = event.rawY
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                moveType = 2
                spacing = getSpacing(event) / 3
                degree = getDegree(event)
            }
            MotionEvent.ACTION_MOVE -> if (moveType == 1) {
                transX = transX + event.rawX - actionX
                transY = transY + event.rawY - actionY
                translationX = transX
                translationY = transY
                actionX = event.rawX
                actionY = event.rawY
                onPositionChangedListener.onPositionChanged(translationX, translationY, scale)
            } else if (moveType == 2) {
                scale = scale * getSpacing(event) / 3 / spacing
                scaleX = scale
                scaleY = scale
                onPositionChangedListener.onPositionChanged(translationX, translationY, scale)
                mRotation = mRotation + getDegree(event) - degree
                if (mRotation > 360) {
                    mRotation -= 360
                }
                if (mRotation < -360) {
                    mRotation += 360
                }
                //                    setRotation(rotation);
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> moveType = 0
        }
        return super.onTouchEvent(event)
    }

    // 触碰两点间距离
    private fun getSpacing(event: MotionEvent): Float {
        //通过三角函数得到两点间的距离
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y.toDouble()).toFloat()
    }

    // 取旋转角度
    private fun getDegree(event: MotionEvent): Float {
        //得到两个手指间的旋转角度
        val delta_x = event.getX(0) - event.getX(1).toDouble()
        val delta_y = event.getY(0) - event.getY(1).toDouble()
        val radians = Math.atan2(delta_y, delta_x)
        return Math.toDegrees(radians).toFloat()
    }

    internal interface OnPositionChangedListener {
        fun onPositionChanged(translationX: Float, translationY: Float, scale: Float)
    }

    init {
        isClickable = true
    }
}