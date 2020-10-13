package com.cbsd.libra.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.VideoView
import com.cbsd.libra.R
import com.cbsd.libra.model.ViewPosition

class DragView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    var mVideoView: VideoView?
    private val mPushView: ImageView?
    private val _1dp: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        1f,
        context.resources.displayMetrics
    )
    private var mCenterInParent = false
    private var mImageDrawable: Drawable? = null
    private var mPushImageDrawable: Drawable? = null
    private var mImageHeight = 0f
    private var mImageWidth = 0f
    private var mPushImageHeight = 0f
    private var mPushImageWidth = 0f
    var mLeft = 0
    var mTop = 0
    private var mIconVisibility = true

    /**
     * callback interface to be invoked when the image view has clicked
     */
    private val mOnImageViewClickListener: OnImageViewClickListener? = null
    fun showIconAndBorder() {
        if (null != mPushView) {
            mPushView.visibility = VISIBLE
        }
        if (null != mVideoView) {
            mVideoView!!.setBackgroundDrawable(context.resources.getDrawable(R.drawable.paster_image_border))
        }
        mIconVisibility = true
    }

    private fun parseAttr(context: Context, attrs: AttributeSet?) {
        if (null == attrs) return
        val a = context.obtainStyledAttributes(attrs, R.styleable.DragView)
        if (a != null) {
            val n = a.indexCount
            for (i in 0 until n) {
                val attr = a.getIndex(i)
                if (attr == R.styleable.DragView_centerInParent) {
                    mCenterInParent = a.getBoolean(attr, false)
                } else if (attr == R.styleable.DragView_image) {
                    mImageDrawable = a.getDrawable(attr)
                } else if (attr == R.styleable.DragView_image_height) {
                    mImageHeight = a.getDimension(attr, 200 * _1dp)
                } else if (attr == R.styleable.DragView_image_width) {
                    mImageWidth = a.getDimension(attr, 200 * _1dp)
                } else if (attr == R.styleable.DragView_push_image) {
                    mPushImageDrawable = a.getDrawable(attr)
                } else if (attr == R.styleable.DragView_push_image_width) {
                    mPushImageWidth = a.getDimension(attr, 50 * _1dp)
                } else if (attr == R.styleable.DragView_push_image_height) {
                    mPushImageHeight = a.getDimension(attr, 50 * _1dp)
                } else if (attr == R.styleable.DragView_left) {
                    mLeft = a.getDimension(attr, 0 * _1dp).toInt()
                } else if (attr == R.styleable.DragView_top) {
                    mTop = a.getDimension(attr, 0 * _1dp).toInt()
                }
            }
        }
    }

    private fun setViewToAttr(pWidth: Int, pHeight: Int) {
        if (null != mImageDrawable) {
//            mView!!.setBackgroundDrawable(mImageDrawable)
        }
        if (null != mPushImageDrawable) {
            mPushView!!.setBackgroundDrawable(mPushImageDrawable)
        }
        val viewLP = mVideoView!!.layoutParams as FrameLayout.LayoutParams
        viewLP.width = mImageWidth.toInt()
        viewLP.height = mImageHeight.toInt()
        var left = 0
        var top = 0
        if (mCenterInParent) {
            left = pWidth / 2 - viewLP.width / 2
            top = pHeight / 2 - viewLP.height / 2
        } else {
            if (mLeft > 0) left = mLeft
            if (mTop > 0) top = mTop
        }
        viewLP.leftMargin = left
        viewLP.topMargin = top
        mVideoView!!.layoutParams = viewLP
        val pushViewLP = mPushView!!.layoutParams as FrameLayout.LayoutParams
        pushViewLP.width = mPushImageWidth.toInt()
        pushViewLP.height = mPushImageHeight.toInt()
        pushViewLP.leftMargin = (viewLP.leftMargin + mImageWidth - mPushImageWidth / 2).toInt()
        pushViewLP.topMargin = (viewLP.topMargin + mImageHeight - mPushImageHeight / 2).toInt()
        mPushView.layoutParams = pushViewLP
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setParamsForView(widthMeasureSpec, heightMeasureSpec)
    }

    private var hasSetParamsForView = false
    private fun setParamsForView(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val layoutParams = layoutParams
        if (null != layoutParams && !hasSetParamsForView) {
            hasSetParamsForView = true
            val width: Int = if (getLayoutParams().width == LayoutParams.MATCH_PARENT) {
                MeasureSpec.getSize(widthMeasureSpec)
            } else {
                getLayoutParams().width
            }
            val height: Int = if (getLayoutParams().height == LayoutParams.MATCH_PARENT) {
                    MeasureSpec.getSize(heightMeasureSpec)
                } else {
                    getLayoutParams().height
                }
            setViewToAttr(width, height)
            Log.d("tag", "width :$width height :$height")
        }
    }

    /**
     * Interface definition for a callback to be invoked when the image view has clicked
     */
    interface OnImageViewClickListener {
        /**
         * callback method to be invoked when the image view has clicked
         */
        fun onImageViewClick(singleFingerView: DragView?)
    }

    /**
     * call to change the index of imageview
     */
    fun changeViewIndexToTop() {
        //if the view clicked then notify listener if there is one
        mOnImageViewClickListener?.onImageViewClick(this)
    }

    init {
        parseAttr(context, attrs)
        val mRoot = inflate(context, R.layout.test_image_view, null)
        addView(mRoot, -1, -1)
        mPushView = mRoot.findViewById<View>(R.id.push_view) as ImageView
        mVideoView = mRoot.findViewById<View>(R.id.view) as VideoView
        mVideoView!!.setTag(R.id.single_finger_view_scale, 1.0.toFloat())
        mPushView!!.setOnTouchListener(PushTouchListener(mVideoView!!))
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val mediaPlayer = arrayOfNulls<MediaPlayer>(1)
        mVideoView!!.setOnInfoListener { mp, i, i2 ->
            mediaPlayer[0] = mp
            mp.isLooping = true
            false
        }
        mVideoView!!.setOnTouchListener(
            ViewOnTouchListener(
                mPushView,
                wm.defaultDisplay.width,
                wm.defaultDisplay.height
            )
        )
    }

    fun setVideoURI(uriPath: String) {
        setVideoURI(Uri.parse(uriPath))
    }

    fun setVideoURI(uri: Uri) {
        mVideoView!!.setVideoURI(uri)
    }

    fun pause(){
        mVideoView!!.pause()
    }
    fun resume(){
        mVideoView!!.resume()
    }
    fun start(){
        mVideoView!!.start()
    }
    fun stopPlayback(){
        mVideoView!!.stopPlayback()
    }

    private var viewPosition: ViewPosition = ViewPosition(left, top, right, bottom, width, height)
    private var onViewPositionChangedListener: OnViewPositionChangedListener? = null

    interface OnViewPositionChangedListener {
        fun onViewPositionChanged(viewPosition: ViewPosition)
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
}