package com.cbsd.libra.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import android.widget.VideoView
import com.cbsd.libra.model.ViewPosition
import com.cbsd.libra.utils.LogUtils
import kotlin.math.sqrt

class DragVideoView : VideoView, ScaleGestureDetector.OnScaleGestureListener{

    private var lastX = 0
    private var lastY = 0
    private var startScaleX = 0F
    private var startScaleY = 0F


    private var viewPosition: ViewPosition = ViewPosition(left, top, right, bottom, width, height)

    private var onViewPositionChangedListener: OnViewPositionChangedListener? = null
    private var onScaleChangedListener: OnScaleChangedListener? = null
    private var gestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)

    constructor(mContext: Context, attrs: AttributeSet?) : super(
        mContext,
        attrs
    )

    constructor(mContext: Context) : super(mContext)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = View.getDefaultSize(width, widthMeasureSpec)
        val h = View.getDefaultSize(width, heightMeasureSpec)
        setMeasuredDimension(w, h)
    }
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        val cnt = ev!!.pointerCount
        when (cnt) {
            1 -> {
                //单点 拖动
                val startX = ev.x.toInt()
                val startY = ev.y.toInt()
                when (ev.action) {
                    MotionEvent.ACTION_UP -> {
                        val lp = layoutParams as FrameLayout.LayoutParams
                        lp.leftMargin = viewPosition.left
                        lp.topMargin = viewPosition.top
                        lp.rightMargin = viewPosition.right
                        lp.bottomMargin = viewPosition.bottom
                        layoutParams = lp
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val offsetX = startX - lastX
                        val offsetY = startY - lastY
                        val lp = layoutParams as FrameLayout.LayoutParams
                        lp.leftMargin += offsetX
                        lp.topMargin += offsetY
                        lp.rightMargin += offsetX
                        lp.bottomMargin += offsetY

                        viewPosition.left = lp.leftMargin
                        viewPosition.top = lp.topMargin
                        viewPosition.right = lp.rightMargin
                        viewPosition.bottom = lp.bottomMargin
                        onViewPositionChangedListener?.onViewPositionChanged(viewPosition)
                        layoutParams = lp
                    }
                    MotionEvent.ACTION_DOWN -> {
                        lastX = startX
                        lastY = startX
                    }
                }
            }
            2 -> {
//                //双点 缩放
//                when (ev.action) {
//                    MotionEvent.ACTION_POINTER_DOWN -> {
//                        spacing = getSpacing(ev)
//                        degree = getDegree(ev)
//                        LogUtils.d("ACTION_POINTER_DOWN")
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        scale = scale * getSpacing(ev) / spacing
//                        if (scale < -3.4)
//                            scale = -3.5F
//                        if (scale > 3.402)
//                            scale = 3.4F
//                        LogUtils.d("缩放scale:$scale , ${getSpacing(ev)}, $spacing")
//                        scaleX = scale
//                        scaleY = scale
////                        rota = rota + getDegree(ev) - degree
////                        if (rota > 360){
////                            rota -= 360
////                        }
////                        if (rota < -360){
////                            rota += 360
////                        }
////                        rotation = rota
//                    }
//                }
            }
        }
        return if (cnt == 1) true else gestureDetector.onTouchEvent(ev)
//        return super.onTouchEvent(ev)
//        return true
    }

    private var spacing = 0f
    private var mScale = 1f // 伸缩比例

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        val factor = detector!!.scaleFactor
        val diffX = detector.currentSpanX - startScaleX
        val diffY = detector.currentSpanY - startScaleY

        mScale = mScale * sqrt(x * x + y * y.toDouble()).toFloat() / 3 / spacing

        val lp = layoutParams as FrameLayout.LayoutParams
        onScaleChangedListener?.onScaleChanged(
            lp.width + diffX.toInt() / 3,
            lp.height + diffY.toInt() / 3
        )
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        val factor = detector!!.scaleFactor
        LogUtils.d("dragVideo onScaleBegin")
        startScaleX = detector.currentSpanX
        startScaleY = detector.currentSpanY
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        LogUtils.d("dragVideo onScaleEnd")
    }

    // 触碰两点间距离
    private fun getSpacing(event: MotionEvent): Float {
        //通过三角函数得到两点间的距离
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt(x * x + y * y.toDouble()).toFloat()
    }

    /**
     * 设置宽度
     */
    fun setWidth(width: Int) {
        viewPosition.width = width
        onViewPositionChangedListener?.onViewPositionChanged(viewPosition)
        val lp = layoutParams as FrameLayout.LayoutParams
        lp.width = viewPosition.width
        layoutParams = lp
    }

    /**
     * 设置高度
     */
    fun setHeight(height: Int) {
        viewPosition.height = height
        onViewPositionChangedListener?.onViewPositionChanged(viewPosition)
        val lp = layoutParams as FrameLayout.LayoutParams
        lp.height = viewPosition.height
        layoutParams = lp
    }

    /**
     * 向左移动
     */
    fun toTheLeft(offset: Int) {
        val lp = layoutParams as FrameLayout.LayoutParams
        val l = lp.leftMargin
        val r = lp.rightMargin
        lp.leftMargin = l - offset
        lp.rightMargin = r - offset
        viewPosition.left = lp.leftMargin
        viewPosition.right = lp.rightMargin
        onViewPositionChangedListener?.onViewPositionChanged(viewPosition)
        layoutParams = lp
    }

    /**
     * 向右移动
     */
    fun toTheRight(offset: Int) {
        val lp = layoutParams as FrameLayout.LayoutParams
        val l = lp.leftMargin
        val r = lp.rightMargin
        lp.leftMargin = l + offset
        lp.rightMargin = r + offset
        viewPosition.left = lp.leftMargin
        viewPosition.right = lp.rightMargin
        onViewPositionChangedListener?.onViewPositionChanged(viewPosition)
        layoutParams = lp
    }

    fun moveToHorizontally(offset: Int) {
        val lp = layoutParams as FrameLayout.LayoutParams
        val l = lp.leftMargin
        val r = lp.rightMargin
        lp.leftMargin = l + offset
        lp.rightMargin = r + offset
        viewPosition.left = lp.leftMargin
        viewPosition.right = lp.rightMargin
        onViewPositionChangedListener?.onViewPositionChanged(viewPosition)
        layoutParams = lp
    }

    /**
     * 向上移动
     */
    fun toTheTop(offset: Int) {
        val lp = layoutParams as FrameLayout.LayoutParams
        val t = lp.topMargin
        val b = lp.bottomMargin
        lp.topMargin = t - offset
        lp.bottomMargin = b - offset
        viewPosition.top = lp.topMargin
        viewPosition.bottom = lp.bottomMargin
        onViewPositionChangedListener?.onViewPositionChanged(viewPosition)
        layoutParams = lp
    }

    /**
     * 向下移动
     */
    fun toTheBottom(offset: Int) {
        val lp = layoutParams as FrameLayout.LayoutParams
        val t = lp.topMargin
        val b = lp.bottomMargin
        lp.topMargin = t + offset
        lp.bottomMargin = b + offset
        viewPosition.top = lp.topMargin
        viewPosition.bottom = lp.bottomMargin
        onViewPositionChangedListener?.onViewPositionChanged(viewPosition)
        layoutParams = lp
    }

    fun moveToVertical(offset: Int) {
        val lp = layoutParams as FrameLayout.LayoutParams
        val t = lp.topMargin
        val b = lp.bottomMargin
        lp.topMargin = t + offset
        lp.bottomMargin = b + offset
        viewPosition.top = lp.topMargin
        viewPosition.bottom = lp.bottomMargin
        onViewPositionChangedListener?.onViewPositionChanged(viewPosition)
        layoutParams = lp
    }

    /**
     * 位置信息改变时回调
     */
    fun setOnViewPositionChangedListener(changed: (viewPosition: ViewPosition) -> Unit) {
        onViewPositionChangedListener = object : OnViewPositionChangedListener {
            override fun onViewPositionChanged(viewPosition: ViewPosition) {
                changed(viewPosition)
            }
        }
    }

    fun setOnScaleChangedListener(scale: (width: Int, height: Int) -> Unit){
        onScaleChangedListener = object : OnScaleChangedListener{
            override fun onScaleChanged(width: Int, height: Int) {
                scale(width, height)
            }
        }
    }

    interface OnViewPositionChangedListener {
        fun onViewPositionChanged(viewPosition: ViewPosition)
    }

    interface OnScaleChangedListener {
        fun onScaleChanged(width: Int, height: Int)
    }
}