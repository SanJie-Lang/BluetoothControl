package com.cbsd.libra.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.cbsd.libra.R

class RadiusImageView(
    private val mContext: Context,
    attrs: AttributeSet?
) : AppCompatImageView(mContext, attrs) {

    /**
     * 用于绘制Layer
     */
    private var paint: Paint? = null
        private set

    /**
     * 用于绘制描边
     */
    private var borderPaint: Paint? = null
        private set

    /**
     * 半径
     */
    private var radius = 0f
    private var topLeftRadius = 0f
    private var topRightRadius = 0f
    private var bottomLeftRadius = 0f
    private var bottomRightRadius = 0f

    /**
     * 不常用
     */
    private var topLeftRadius_x = 0f
    private var topLeftRadius_y = 0f
    private var topRightRadius_x = 0f
    private var topRightRadius_y = 0f
    private var bottomLeftRadius_x = 0f
    private var bottomLeftRadius_y = 0f
    private var bottomRightRadius_x = 0f
    private var bottomRightRadius_y = 0f

    /**
     * 描边
     */
    private var borderWidth = 0f
    private var borderSpace = 0f
    private var borderColor = 0

    /**
     * 是否有弧度，即是否需要绘制圆弧
     */
    private var circle = false
    private var styleType = 0
    private fun initAttrs(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs,
            R.styleable.RadiusImageView
        )
        //  半径
        radius = ta.getDimension(R.styleable.RadiusImageView_radius, 0f)
        topLeftRadius = ta.getDimension(R.styleable.RadiusImageView_topLeftRadius, 0f)
        topRightRadius = ta.getDimension(R.styleable.RadiusImageView_topRightRadius, 0f)
        bottomLeftRadius = ta.getDimension(R.styleable.RadiusImageView_bottomLeftRadius, 0f)
        bottomRightRadius = ta.getDimension(R.styleable.RadiusImageView_bottomRightRadius, 0f)
        //  展示类型
        styleType = ta.getInt(
            R.styleable.RadiusImageView_scaleType,
            TOP
        )
        //  描边
        borderWidth = ta.getDimension(R.styleable.RadiusImageView_borderWidth, 0f)
        borderSpace = ta.getDimension(R.styleable.RadiusImageView_borderSpace, 0f)
        borderColor =
            ta.getColor(R.styleable.RadiusImageView_borderColor, Color.WHITE)

        //  不常用属性
        topLeftRadius_x = ta.getDimension(R.styleable.RadiusImageView_topLeftRadius_x, 0f)
        topLeftRadius_y = ta.getDimension(R.styleable.RadiusImageView_topLeftRadius_y, 0f)
        topRightRadius_x = ta.getDimension(R.styleable.RadiusImageView_topRightRadius_x, 0f)
        topRightRadius_y = ta.getDimension(R.styleable.RadiusImageView_topRightRadius_y, 0f)
        bottomLeftRadius_x = ta.getDimension(R.styleable.RadiusImageView_bottomLeftRadius_x, 0f)
        bottomLeftRadius_y = ta.getDimension(R.styleable.RadiusImageView_bottomLeftRadius_y, 0f)
        bottomRightRadius_x = ta.getDimension(R.styleable.RadiusImageView_bottomRightRadius_x, 0f)
        bottomRightRadius_y = ta.getDimension(R.styleable.RadiusImageView_bottomRightRadius_y, 0f)
        ta.recycle()
        initData()
    }

    fun initData() {
        initRadius()
        //  判断是否需要调用绘制函数
        circle =
            borderWidth != 0f || borderSpace != 0f || topLeftRadius_x != 0f || topLeftRadius_y != 0f || topRightRadius_x != 0f || topRightRadius_y != 0f || bottomLeftRadius_x != 0f || bottomLeftRadius_y != 0f || bottomRightRadius_x != 0f || bottomRightRadius_y != 0f

        //  设置画笔
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint!!.style = Paint.Style.STROKE
        borderPaint!!.strokeWidth = borderWidth
        borderPaint!!.color = borderColor
        if (circle) {
            //  为什么设置这一条，因为Glide中，在into 源码内
            //  不同的 ScaleType 会对drawable进行压缩，一旦压缩了，我们在onDraw里面获取图片的大小就没有意义了
            scaleType = ScaleType.MATRIX
        }
    }

    /**
     * 初始化半径
     */
    private fun initRadius() {
        //  该处便于代码编写 如XML设置 radius = 20，topLeftRadius = 10，最终结果是  10 20 20 20
        if (radius != 0f) {
            topLeftRadius = if (topLeftRadius == 0f) radius else topLeftRadius
            topRightRadius = if (topRightRadius == 0f) radius else topRightRadius
            bottomLeftRadius = if (bottomLeftRadius == 0f) radius else bottomLeftRadius
            bottomRightRadius = if (bottomRightRadius == 0f) radius else bottomRightRadius
        }
        //  如果设置了 radius = 20，topLeftRadius = 10，topLeftRadius_x = 30,
        //  最终结果，topLeftRadius_x = 30，topLeftRadius_y = 10，其余 20
        topLeftRadius_x = if (topLeftRadius_x == 0f) topLeftRadius else topLeftRadius_x
        topLeftRadius_y = if (topLeftRadius_y == 0f) topLeftRadius else topLeftRadius_y
        topRightRadius_x = if (topRightRadius_x == 0f) topRightRadius else topRightRadius_x
        topRightRadius_y = if (topRightRadius_y == 0f) topRightRadius else topRightRadius_y
        bottomLeftRadius_x = if (bottomLeftRadius_x == 0f) bottomLeftRadius else bottomLeftRadius_x
        bottomLeftRadius_y = if (bottomLeftRadius_y == 0f) bottomLeftRadius else bottomLeftRadius_y
        bottomRightRadius_x =
            if (bottomRightRadius_x == 0f) bottomRightRadius else bottomRightRadius_x
        bottomRightRadius_y =
            if (bottomRightRadius_y == 0f) bottomRightRadius else bottomRightRadius_y
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val drawable = drawable

        //  使用局部变量，降低函数调用次数
        val vw = measuredWidth
        val vh = measuredHeight
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom

        //  绘制描边
        if (borderWidth != 0f) {
            val rectF = RectF(
                paddingLeft.toFloat(),
                paddingTop.toFloat(),
                (vw - paddingRight).toFloat(),
                (vh - paddingBottom).toFloat()
            )
            //  描边会有一半处于框体之外
            val i = borderWidth / 2
            //  移动矩形，以便于描边都处于view内
            rectF.inset(i, i)
            //  绘制描边，半径需要进行偏移 i
            drawPath(canvas, rectF, borderPaint, i)
        }
        if (null != drawable && circle) {
            val rectF = RectF(
                paddingLeft.toFloat(),
                paddingTop.toFloat(),
                (vw - paddingRight).toFloat(),
                (vh - paddingBottom).toFloat()
            )
            //  矩形需要缩小的值
            var i = borderWidth + borderSpace
            //  这里解释一下，为什么要减去一个像素，因为像素融合时，由于锯齿的存在和图片像素不高，会导致图片和边框出现1像素的间隙
            //  大家可以试一下，去掉这一句，然后用高清图就不会出问题，用非高清图就会出现
            i = if (i > 1) i - 1 else 0F
            //  矩形偏移
            rectF.inset(i, i)
            val layerId = canvas.saveLayer(rectF, null, Canvas.ALL_SAVE_FLAG)
            //  多边形
            drawPath(canvas, rectF, paint, i)
            //  设置像素融合模式
            paint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            //  drawable转为 bitmap
            val bitmap = drawableToBitmap(drawable)
            //  根据图片的大小，控件的大小，图片的展示形式，然后来计算图片的src取值范围
            val src =
                getSrc(bitmap, rectF.width().toInt(), rectF.height().toInt())
            //  dst取整个控件，也就是表示，我们的图片要占满整个控件
            canvas.drawBitmap(bitmap, src, rectF, paint)
            paint!!.xfermode = null
            canvas.restoreToCount(layerId)
        } else {
            super.onDraw(canvas)
        }
    }

    /**
     * 这里详细说一下，我们的目标就是在 bitmap 中找到一个 和 view 宽高比例相等的 一块矩形 tempRect，然后截取出来 放到整个view中
     * tempRect 总是会存在
     *
     * @param bitmap bitmap
     * @param rw     绘制区域的宽度
     * @param rh     绘制区域的高度
     * @return 矩形
     */
    private fun getSrc(bitmap: Bitmap, rw: Int, rh: Int): Rect {
        //  bw bh,bitmap 的宽高
        //  vw vh,view 的宽高
        val bw = bitmap.width
        val bh = bitmap.height
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0

        //  判断 bw/bh 与 rw/rh
        val temp1 = bw * rh
        val temp2 = rw * bh

        //  相似矩形的宽高
        val tempRect = intArrayOf(bw, bh)
        if (temp1 == temp2) {
            return Rect(0, 0, bw, bh)
        } else if (temp1 > temp2) {
            val tempBw = temp2 / rh
            tempRect[0] = tempBw
        } else if (temp1 < temp2) {
            val tempBh = temp1 / rw
            tempRect[1] = tempBh
        }

        //  tempRect 的宽度与 bw 的比值
        val compare = bw > tempRect[0]
        when (styleType) {
            TOP -> {
                //  从上往下展示，我们这里的效果是不止从上往下，compare = true，还要居中
                left = if (compare) (bw - tempRect[0]) / 2 else 0
                top = 0
                right = if (compare) (bw + tempRect[0]) / 2 else tempRect[0]
                bottom = tempRect[1]
            }
            CENTER -> {
                //  居中
                left = if (compare) (bw - tempRect[0]) / 2 else 0
                top = if (compare) 0 else (bh - tempRect[1]) / 2
                right = if (compare) (bw + tempRect[0]) / 2 else tempRect[0]
                bottom = if (compare) tempRect[1] else (bh + tempRect[1]) / 2
            }
            BOTTOM -> {
                left = if (compare) (bw - tempRect[0]) / 2 else 0
                top = if (compare) 0 else bh - tempRect[1]
                right = if (compare) (bw + tempRect[0]) / 2 else tempRect[0]
                bottom = if (compare) tempRect[1] else bh
            }
            FITXY -> {
                left = 0
                top = 0
                right = bw
                bottom = bh
            }
            else -> {
            }
        }
        return Rect(left, top, right, bottom)
    }

    /**
     * 绘制多边形
     *
     * @param canvas 画布
     * @param rectF  矩形
     * @param paint  画笔
     * @param offset 半径偏移量
     */
    private fun drawPath(
        canvas: Canvas,
        rectF: RectF,
        paint: Paint?,
        offset: Float
    ) {
        val path = Path()
        path.addRoundRect(
            rectF, floatArrayOf(
                offsetRadius(topLeftRadius_x, offset),
                offsetRadius(topLeftRadius_y, offset),
                offsetRadius(topRightRadius_x, offset),
                offsetRadius(topRightRadius_y, offset),
                offsetRadius(bottomRightRadius_x, offset),
                offsetRadius(bottomRightRadius_y, offset),
                offsetRadius(bottomLeftRadius_x, offset),
                offsetRadius(bottomLeftRadius_y, offset)
            ), Path.Direction.CW
        )
        path.close()
        canvas.drawPath(path, paint!!)
    }

    /**
     * 计算半径偏移值，必须大于等于0
     *
     * @param radius 半径
     * @param offset 偏移量
     * @return 偏移半径
     */
    private fun offsetRadius(radius: Float, offset: Float): Float {
        return Math.max(radius - offset, 0f)
    }

    /**
     * drawable 转 bitmap
     * 这个函数可以放在Util类里面，算是一个公共函数
     *
     * @param drawable 要转换的Drawable
     * @return 转换完成的Bitmap
     */
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val w = drawable.intrinsicWidth
        val h = drawable.intrinsicHeight
        val config =
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        val bitmap = Bitmap.createBitmap(w, h, config)
        //注意，下面三行代码要用到，否则在View或者SurfaceView里的canvas.drawBitmap会看不到图
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        return bitmap
    }

    /////////////////////////////  set/get   /////////////////////////////
    fun setRadius(radius: Float) {
        setTopLeftRadius(radius)
        setTopRightRadius(radius)
        setBottomLeftRadius(radius)
        setBottomRightRadius(radius)
    }

    fun setTopLeftRadius(topLeftRadius: Float) {
        setTopLeftRadius_x(topLeftRadius)
        setTopLeftRadius_y(topLeftRadius)
    }

    fun setTopRightRadius(topRightRadius: Float) {
        setTopRightRadius_x(topRightRadius)
        setTopRightRadius_y(topRightRadius)
    }

    fun setBottomLeftRadius(bottomLeftRadius: Float) {
        setBottomLeftRadius_x(bottomLeftRadius)
        setBottomLeftRadius_y(bottomLeftRadius)
    }

    fun setBottomRightRadius(bottomRightRadius: Float) {
        setBottomRightRadius_x(bottomRightRadius)
        setBottomRightRadius_y(bottomRightRadius)
    }

    fun setStyleType(styleType: Int) {
        this.styleType = styleType
    }

    fun setCircle(circle: Boolean) {
        this.circle = circle
    }

    fun setBorderWidth(borderWidth: Float) {
        this.borderWidth = borderWidth
        borderPaint!!.strokeWidth = borderWidth
    }

    fun setBorderSpace(borderSpace: Float) {
        this.borderSpace = borderSpace
    }

    fun setBorderColor(borderColor: Int) {
        this.borderColor = borderColor
        borderPaint!!.color = borderColor
    }

    fun setTopLeftRadius_x(topLeftRadius_x: Float) {
        this.topLeftRadius_x = topLeftRadius_x
    }

    fun setTopLeftRadius_y(topLeftRadius_y: Float) {
        this.topLeftRadius_y = topLeftRadius_y
    }

    fun setTopRightRadius_x(topRightRadius_x: Float) {
        this.topRightRadius_x = topRightRadius_x
    }

    fun setTopRightRadius_y(topRightRadius_y: Float) {
        this.topRightRadius_y = topRightRadius_y
    }

    fun setBottomLeftRadius_x(bottomLeftRadius_x: Float) {
        this.bottomLeftRadius_x = bottomLeftRadius_x
    }

    fun setBottomLeftRadius_y(bottomLeftRadius_y: Float) {
        this.bottomLeftRadius_y = bottomLeftRadius_y
    }

    fun setBottomRightRadius_x(bottomRightRadius_x: Float) {
        this.bottomRightRadius_x = bottomRightRadius_x
    }

    fun setBottomRightRadius_y(bottomRightRadius_y: Float) {
        this.bottomRightRadius_y = bottomRightRadius_y
    }

    companion object {
        /**
         * 图片展示方式
         * 0 -- 图片顶部开始展示，铺满，如果Y轴铺满时，X轴大，则图片水平居中
         * 1 -- 图片中心点与指定区域中心重合
         * 2 -- 图片底部开始展示，铺满，如果Y轴铺满时，X轴大，则图片水平居中
         * 3 -- 图片完全展示
         */
        const val TOP = 0
        const val CENTER = 1
        const val BOTTOM = 2
        const val FITXY = 3
    }

    init {
        initAttrs(attrs)
    }
}