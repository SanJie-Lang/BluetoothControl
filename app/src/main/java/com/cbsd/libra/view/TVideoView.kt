package com.cbsd.libra.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.VideoView

/**
 * 全屏
 */
class TVideoView : VideoView {
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
}