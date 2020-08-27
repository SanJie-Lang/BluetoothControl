package com.sanjie.videoplayer

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.sanjie.videoplayer.NiceUtil.scanForActivity
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Created by SanJie on 2019.04.16
 * 控制器抽象类
 */
abstract class VideoPlayerController(private val mContext: Context) : FrameLayout(mContext), View.OnTouchListener {
    @JvmField
    protected var mVideoPlayer: IVideoPlayer? = null
    private var mUpdateProgressTimer: Timer? = null
    private var mUpdateProgressTimerTask: TimerTask? = null
    private var mDownX = 0f
    private var mDownY = 0f
    private var mNeedChangePosition = false
    private var mNeedChangeVolume = false
    private var mNeedChangeBrightness = false
    private var mGestureDownPosition: Long = 0
    private var mGestureDownBrightness = 0f
    private var mGestureDownVolume = 0
    private var mNewPosition: Long = 0
    open fun setVideoPlayer(videoPlayer: IVideoPlayer?) {
        mVideoPlayer = videoPlayer
    }

    /**
     * 设置播放的视频的标题
     *
     * @param title 视频标题
     */
    abstract fun setTitle(title: String?)

    /**
     * 视频底图
     *
     * @param resId 视频底图资源
     */
    abstract fun setImage(@DrawableRes resId: Int)

    /**
     * 视频底图ImageView控件，提供给外部用图片加载工具来加载网络图片
     *
     * @return 底图ImageView
     */
    abstract fun imageView(): ImageView?

    /**
     * 设置总时长.
     */
    abstract fun setLength(length: Long)

    /**
     * 当播放器的播放状态发生变化，在此方法中国你更新不同的播放状态的UI
     *
     * @param playState 播放状态：
     *
     *  * [VideoPlayer.STATE_IDLE]
     *  * [VideoPlayer.STATE_PREPARING]
     *  * [VideoPlayer.STATE_PREPARED]
     *  * [VideoPlayer.STATE_PLAYING]
     *  * [VideoPlayer.STATE_PAUSED]
     *  * [VideoPlayer.STATE_BUFFERING_PLAYING]
     *  * [VideoPlayer.STATE_BUFFERING_PAUSED]
     *  * [VideoPlayer.STATE_ERROR]
     *  * [VideoPlayer.STATE_COMPLETED]
     *
     */
    abstract fun onPlayStateChanged(playState: Int)

    /**
     * 当播放器的播放模式发生变化，在此方法中更新不同模式下的控制器界面。
     *
     * @param playMode 播放器的模式：
     *
     *  * [VideoPlayer.MODE_NORMAL]
     *  * [VideoPlayer.MODE_FULL_SCREEN]
     *  * [VideoPlayer.MODE_TINY_WINDOW]
     *
     */
    abstract fun onPlayModeChanged(playMode: Int)

    /**
     * 重置控制器，将控制器恢复到初始状态。
     */
    abstract fun reset()

    /**
     * 开启更新进度的计时器。
     */
    protected fun startUpdateProgressTimer() {
        cancelUpdateProgressTimer()
        if (mUpdateProgressTimer == null) {
            mUpdateProgressTimer = Timer()
        }
        if (mUpdateProgressTimerTask == null) {
            mUpdateProgressTimerTask = object : TimerTask() {
                override fun run() {
                    post { updateProgress() }
                }
            }
        }
        mUpdateProgressTimer!!.schedule(mUpdateProgressTimerTask, 0, 1000)
    }

    /**
     * 取消更新进度的计时器。
     */
    protected fun cancelUpdateProgressTimer() {
        if (mUpdateProgressTimer != null) {
            mUpdateProgressTimer!!.cancel()
            mUpdateProgressTimer = null
        }
        if (mUpdateProgressTimerTask != null) {
            mUpdateProgressTimerTask!!.cancel()
            mUpdateProgressTimerTask = null
        }
    }

    /**
     * 更新进度，包括进度条进度，展示的当前播放位置时长，总时长等。
     */
    protected abstract fun updateProgress()
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        // 只有全屏的时候才能拖动位置、亮度、声音
        if (!mVideoPlayer!!.isFullScreen) {
            return false
        }
        // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
        if (mVideoPlayer!!.isIdle
                || mVideoPlayer!!.isError
                || mVideoPlayer!!.isPreparing
                || mVideoPlayer!!.isPrepared
                || mVideoPlayer!!.isCompleted) {
            hideChangePosition()
            hideChangeBrightness()
            hideChangeVolume()
            return false
        }
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = x
                mDownY = y
                mNeedChangePosition = false
                mNeedChangeVolume = false
                mNeedChangeBrightness = false
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - mDownX
                var deltaY = y - mDownY
                val absDeltaX = Math.abs(deltaX)
                val absDeltaY = Math.abs(deltaY)
                if (!mNeedChangePosition && !mNeedChangeVolume && !mNeedChangeBrightness) {
                    // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
                    if (absDeltaX >= THRESHOLD) {
                        cancelUpdateProgressTimer()
                        mNeedChangePosition = true
                        mGestureDownPosition = mVideoPlayer!!.currentPosition
                    } else if (absDeltaY >= THRESHOLD) {
                        if (mDownX < width * 0.5f) {
                            // 左侧改变亮度
                            mNeedChangeBrightness = true
                            mGestureDownBrightness = scanForActivity(mContext)!!
                                    .window.attributes.screenBrightness
                        } else {
                            // 右侧改变声音
                            mNeedChangeVolume = true
                            mGestureDownVolume = mVideoPlayer!!.volume
                        }
                    }
                }
                if (mNeedChangePosition) {
                    val duration = mVideoPlayer!!.duration
                    val toPosition = (mGestureDownPosition + duration * deltaX / width).toLong()
                    mNewPosition = max(0, min(duration, toPosition))
                    val newPositionProgress = (100f * mNewPosition / duration).toInt()
                    showChangePosition(duration, newPositionProgress)
                }
                if (mNeedChangeBrightness) {
                    deltaY = -deltaY
                    val deltaBrightness = deltaY * 3 / height
                    var newBrightness = mGestureDownBrightness + deltaBrightness
                    newBrightness = Math.max(0f, Math.min(newBrightness, 1f))
                    val newBrightnessPercentage = newBrightness
                    val params = scanForActivity(mContext)!!
                            .window.attributes
                    params.screenBrightness = newBrightnessPercentage
                    scanForActivity(mContext)!!.window.attributes = params
                    val newBrightnessProgress = (100f * newBrightnessPercentage).toInt()
                    showChangeBrightness(newBrightnessProgress)
                }
                if (mNeedChangeVolume) {
                    deltaY = -deltaY
                    val maxVolume = mVideoPlayer!!.maxVolume
                    val deltaVolume = (maxVolume * deltaY * 3 / height).toInt()
                    var newVolume = mGestureDownVolume + deltaVolume
                    newVolume = Math.max(0, Math.min(maxVolume, newVolume))
                    mVideoPlayer!!.volume = newVolume
                    val newVolumeProgress = (100f * newVolume / maxVolume).toInt()
                    showChangeVolume(newVolumeProgress)
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (mNeedChangePosition) {
                    mVideoPlayer!!.seekTo(mNewPosition)
                    hideChangePosition()
                    startUpdateProgressTimer()
                    return true
                }
                if (mNeedChangeBrightness) {
                    hideChangeBrightness()
                    return true
                }
                if (mNeedChangeVolume) {
                    hideChangeVolume()
                    return true
                }
            }
        }
        return false
    }

    /**
     * 手势左右滑动改变播放位置时，显示控制器中间的播放位置变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param duration            视频总时长ms
     * @param newPositionProgress 新的位置进度，取值0到100。
     */
    protected abstract fun showChangePosition(duration: Long, newPositionProgress: Int)

    /**
     * 手势左右滑动改变播放位置后，手势up或者cancel时，隐藏控制器中间的播放位置变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract fun hideChangePosition()

    /**
     * 手势在右侧上下滑动改变音量时，显示控制器中间的音量变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param newVolumeProgress 新的音量进度，取值1到100。
     */
    protected abstract fun showChangeVolume(newVolumeProgress: Int)

    /**
     * 手势在左侧上下滑动改变音量后，手势up或者cancel时，隐藏控制器中间的音量变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract fun hideChangeVolume()

    /**
     * 手势在左侧上下滑动改变亮度时，显示控制器中间的亮度变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param newBrightnessProgress 新的亮度进度，取值1到100。
     */
    protected abstract fun showChangeBrightness(newBrightnessProgress: Int)

    /**
     * 手势在左侧上下滑动改变亮度后，手势up或者cancel时，隐藏控制器中间的亮度变化视图，
     * 在手势ACTION_UP或ACTION_CANCEL时调用。
     */
    protected abstract fun hideChangeBrightness()

    companion object {
        private const val THRESHOLD = 80
    }

    init {
        setOnTouchListener(this)
    }
}