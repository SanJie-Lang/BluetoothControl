package com.sanjie.videoplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.DrawableRes
import com.example.videoplayer.R
import com.sanjie.videoplayer.ChangeClarityDialog.OnClarityChangedListener
import com.sanjie.videoplayer.NiceUtil.formatTime
import kotlinx.android.synthetic.main.tx_video_palyer_controller.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by SanJie on 2019.04.16
 * 仿腾讯视频热点列表页播放器控制器.
 */
class TxVideoPlayerController(private val mContext: Context) : VideoPlayerController(mContext), View.OnClickListener, SeekBar.OnSeekBarChangeListener, OnClarityChangedListener {
    private var topBottomVisible = false
    private var mDismissTopBottomCountDownTimer: CountDownTimer? = null
    private var clarities: List<Clarity>? = null
    private var defaultClarityIndex = 0
    private var mClarityDialog: ChangeClarityDialog? = null
    private var hasRegisterBatteryReceiver = false // 是否已经注册了电池广播 = false

    private fun init() {
        LayoutInflater.from(mContext).inflate(R.layout.tx_video_palyer_controller, this, true)
        center_start!!.setOnClickListener(this)
        back!!.setOnClickListener(this)
        mRestartPause!!.setOnClickListener(this)
        mFullScreen!!.setOnClickListener(this)
        mClarity!!.setOnClickListener(this)
        mRetry!!.setOnClickListener(this)
        mReplay!!.setOnClickListener(this)
        mShare!!.setOnClickListener(this)
        mSeek!!.setOnSeekBarChangeListener(this)
        setOnClickListener(this)
    }

    override fun setTitle(title: String?) {
        mTitle!!.text = title
    }

    override fun imageView(): ImageView? {
        return mImage
    }

    override fun setImage(@DrawableRes resId: Int) {
        mImage!!.setImageResource(resId)
    }

    override fun setLength(length: Long) {
        mLength!!.text = formatTime(length)
    }

    override fun setVideoPlayer(videoPlayer: IVideoPlayer?) {
        super.setVideoPlayer(videoPlayer)
        // 给播放器配置视频链接地址
        if (clarities != null && clarities!!.size > 1) {
            mVideoPlayer!!.setUp(clarities!![defaultClarityIndex].videoUrl, null)
        }
    }

    /**
     * 设置清晰度
     *
     * @param clarities 清晰度及链接
     */
    fun setClarity(clarities: List<Clarity>?, defaultClarityIndex: Int) {
        if (clarities != null && clarities.size > 1) {
            this.clarities = clarities
            this.defaultClarityIndex = defaultClarityIndex
            val clarityGrades: MutableList<String?> = ArrayList()
            for (clarity in clarities) {
                clarityGrades.add(clarity.grade + " " + clarity.p)
            }
            mClarity!!.text = clarities[defaultClarityIndex].grade
            // 初始化切换清晰度对话框
            mClarityDialog = ChangeClarityDialog(mContext)
            mClarityDialog!!.setClarityGrade(clarityGrades, defaultClarityIndex)
            mClarityDialog!!.setOnClarityCheckedListener(this)
            // 给播放器配置视频链接地址
            if (mVideoPlayer != null) {
                mVideoPlayer!!.setUp(clarities[defaultClarityIndex].videoUrl, null)
            }
        }
    }

    override fun onPlayStateChanged(playState: Int) {
        when (playState) {
            VideoPlayer.STATE_IDLE -> {
            }
            VideoPlayer.STATE_PREPARING -> {
                mImage!!.visibility = View.GONE
                mLoading!!.visibility = View.VISIBLE
                mLoadText!!.text = "正在准备..."
                mError!!.visibility = View.GONE
                mCompleted!!.visibility = View.GONE
                mTop!!.visibility = View.GONE
                mBottom!!.visibility = View.GONE
                center_start!!.visibility = View.GONE
//                mLength!!.visibility = View.GONE
            }
            VideoPlayer.STATE_PREPARED -> startUpdateProgressTimer()
            VideoPlayer.STATE_PLAYING -> {
                mLoading!!.visibility = View.GONE
                mRestartPause!!.setImageResource(R.drawable.ic_player_pause)
                startDismissTopBottomTimer()
            }
            VideoPlayer.STATE_PAUSED -> {
                mLoading!!.visibility = View.GONE
                mRestartPause!!.setImageResource(R.drawable.ic_player_start)
                cancelDismissTopBottomTimer()
            }
            VideoPlayer.STATE_BUFFERING_PLAYING -> {
                mLoading!!.visibility = View.VISIBLE
                mRestartPause!!.setImageResource(R.drawable.ic_player_pause)
                mLoadText!!.text = "正在缓冲..."
                startDismissTopBottomTimer()
            }
            VideoPlayer.STATE_BUFFERING_PAUSED -> {
                mLoading!!.visibility = View.VISIBLE
                mRestartPause!!.setImageResource(R.drawable.ic_player_start)
                mLoadText!!.text = "正在缓冲..."
                cancelDismissTopBottomTimer()
            }
            VideoPlayer.STATE_ERROR -> {
                cancelUpdateProgressTimer()
                setTopBottomVisible(false)
                mTop!!.visibility = View.VISIBLE
                mError!!.visibility = View.VISIBLE
            }
            VideoPlayer.STATE_COMPLETED -> {
                cancelUpdateProgressTimer()
                setTopBottomVisible(false)
                mImage!!.visibility = View.VISIBLE
                mCompleted!!.visibility = View.VISIBLE
            }
        }
    }

    override fun onPlayModeChanged(playMode: Int) {
        when (playMode) {
            VideoPlayer.MODE_NORMAL -> {
                back!!.visibility = View.GONE
                mFullScreen!!.setImageResource(R.drawable.ic_player_enlarge)
                mFullScreen!!.visibility = View.VISIBLE
                mClarity!!.visibility = View.GONE
                battery_time!!.visibility = View.GONE
                if (hasRegisterBatteryReceiver) {
                    mContext.unregisterReceiver(mBatterReceiver)
                    hasRegisterBatteryReceiver = false
                }
            }
            VideoPlayer.MODE_FULL_SCREEN -> {
                back!!.visibility = View.VISIBLE
                mFullScreen!!.visibility = View.GONE
                mFullScreen!!.setImageResource(R.drawable.ic_player_shrink)
                if (clarities != null && clarities!!.size > 1) {
                    mClarity!!.visibility = View.VISIBLE
                }
                battery_time!!.visibility = View.VISIBLE
                if (!hasRegisterBatteryReceiver) {
                    mContext.registerReceiver(mBatterReceiver,
                            IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    hasRegisterBatteryReceiver = true
                }
            }
            VideoPlayer.MODE_TINY_WINDOW -> {
                back!!.visibility = View.VISIBLE
                mClarity!!.visibility = View.GONE
            }
        }
    }

    /**
     * 电池状态即电量变化广播接收器
     */
    private val mBatterReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN)
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                // 充电中
                battery!!.setImageResource(R.drawable.battery_charging)
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                // 充电完成
                battery!!.setImageResource(R.drawable.battery_full)
            } else {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
                val percentage = (level.toFloat() / scale * 100).toInt()
                when {
                    percentage <= 10 -> {
                        battery!!.setImageResource(R.drawable.battery_10)
                    }
                    percentage <= 20 -> {
                        battery!!.setImageResource(R.drawable.battery_20)
                    }
                    percentage <= 50 -> {
                        battery!!.setImageResource(R.drawable.battery_50)
                    }
                    percentage <= 80 -> {
                        battery!!.setImageResource(R.drawable.battery_80)
                    }
                    percentage <= 100 -> {
                        battery!!.setImageResource(R.drawable.battery_100)
                    }
                }
            }
        }
    }

    override fun reset() {
        topBottomVisible = false
        cancelUpdateProgressTimer()
        cancelDismissTopBottomTimer()
        mSeek!!.progress = 0
        mSeek!!.secondaryProgress = 0
        center_start!!.visibility = View.VISIBLE
        mImage!!.visibility = View.VISIBLE
        mBottom!!.visibility = View.GONE
        mFullScreen!!.setImageResource(R.drawable.ic_player_enlarge)
//        mLength!!.visibility = View.VISIBLE
        mTop!!.visibility = View.VISIBLE
        back!!.visibility = View.GONE
        mLoading!!.visibility = View.GONE
        mError!!.visibility = View.GONE
        mCompleted!!.visibility = View.GONE
    }

    /**
     * 尽量不要在onClick中直接处理控件的隐藏、显示及各种UI逻辑。
     * UI相关的逻辑都尽量到[.onPlayStateChanged]和[.onPlayModeChanged]中处理.
     */
    override fun onClick(v: View) {
        if (v === center_start) {
            if (mVideoPlayer!!.isIdle) {
                mVideoPlayer!!.start()
            }
        } else if (v === back) {
            if (mVideoPlayer!!.isFullScreen) {
                mVideoPlayer!!.exitFullScreen()
            } else if (mVideoPlayer!!.isTinyWindow) {
                mVideoPlayer!!.exitTinyWindow()
            }
        } else if (v === mRestartPause) {
            if (mVideoPlayer!!.isPlaying || mVideoPlayer!!.isBufferingPlaying) {
                mVideoPlayer!!.pause()
            } else if (mVideoPlayer!!.isPaused || mVideoPlayer!!.isBufferingPaused) {
                mVideoPlayer!!.restart()
            }
        } else if (v === mFullScreen) {
            if (mVideoPlayer!!.isNormal || mVideoPlayer!!.isTinyWindow) {
                mVideoPlayer!!.enterFullScreen()
            } else if (mVideoPlayer!!.isFullScreen) {
                mVideoPlayer!!.exitFullScreen()
            }
        } else if (v === mClarity) {
            setTopBottomVisible(false) // 隐藏top、bottom
            mClarityDialog!!.show() // 显示清晰度对话框
        } else if (v === mRetry) {
            mVideoPlayer!!.restart()
        } else if (v === mReplay) {
            mRetry!!.performClick()
        } else if (v === mShare) {
            Toast.makeText(mContext, "分享", Toast.LENGTH_SHORT).show()
        } else if (v === this) {
            if (mVideoPlayer!!.isPlaying
                    || mVideoPlayer!!.isPaused
                    || mVideoPlayer!!.isBufferingPlaying
                    || mVideoPlayer!!.isBufferingPaused) {
                setTopBottomVisible(!topBottomVisible)
            }
        }
    }

    override fun onClarityChanged(clarityIndex: Int) {
        // 根据切换后的清晰度索引值，设置对应的视频链接地址，并从当前播放位置接着播放
        val clarity = clarities!![clarityIndex]
        mClarity!!.text = clarity.grade
        val currentPosition = mVideoPlayer!!.currentPosition
        mVideoPlayer!!.releasePlayer()
        mVideoPlayer!!.setUp(clarity.videoUrl, null)
        mVideoPlayer!!.start(currentPosition)
    }

    override fun onClarityNotChanged() {
        // 清晰度没有变化，对话框消失后，需要重新显示出top、bottom
        setTopBottomVisible(true)
    }

    /**
     * 设置top、bottom的显示和隐藏
     *
     * @param visible true显示，false隐藏.
     */
    private fun setTopBottomVisible(visible: Boolean) {
        mTop!!.visibility = if (visible) View.VISIBLE else View.GONE
        mBottom!!.visibility = if (visible) View.VISIBLE else View.GONE
        topBottomVisible = visible
        if (visible) {
            if (!mVideoPlayer!!.isPaused && !mVideoPlayer!!.isBufferingPaused) {
                startDismissTopBottomTimer()
            }
        } else {
            cancelDismissTopBottomTimer()
        }
    }

    /**
     * 开启top、bottom自动消失的timer
     */
    private fun startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer()
        if (mDismissTopBottomCountDownTimer == null) {
            mDismissTopBottomCountDownTimer = object : CountDownTimer(8000, 8000) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    setTopBottomVisible(false)
                }
            }
        }
        mDismissTopBottomCountDownTimer!!.start()
    }

    /**
     * 取消top、bottom自动消失的timer
     */
    private fun cancelDismissTopBottomTimer() {
        if (mDismissTopBottomCountDownTimer != null) {
            mDismissTopBottomCountDownTimer!!.cancel()
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        if (mVideoPlayer!!.isBufferingPaused || mVideoPlayer!!.isPaused) {
            mVideoPlayer!!.restart()
        }
        val position = (mVideoPlayer!!.duration * seekBar.progress / 100f).toLong()
        mVideoPlayer!!.seekTo(position)
        startDismissTopBottomTimer()
    }

    override fun updateProgress() {
        val position = mVideoPlayer!!.currentPosition
        val duration = mVideoPlayer!!.duration
        val bufferPercentage = mVideoPlayer!!.bufferPercentage
        mSeek!!.secondaryProgress = bufferPercentage
        val progress = (100f * position / duration).toInt()
        mSeek!!.progress = progress
        mPosition!!.text = formatTime(position)
        mDuration!!.text = formatTime(duration)
        mLength!!.text = formatTime(duration)
        // 更新时间
        time!!.text = SimpleDateFormat("HH:mm", Locale.CHINA).format(Date())
    }

    override fun showChangePosition(duration: Long, newPositionProgress: Int) {
        mChangePositon!!.visibility = View.VISIBLE
        val newPosition = (duration * newPositionProgress / 100f).toLong()
        mChangePositionCurrent!!.text = formatTime(newPosition)
        mChangePositionProgress!!.progress = newPositionProgress
        mSeek!!.progress = newPositionProgress
        mPosition!!.text = formatTime(newPosition)
    }

    override fun hideChangePosition() {
        mChangePositon!!.visibility = View.GONE
    }

    override fun showChangeVolume(newVolumeProgress: Int) {
        mChangeVolume!!.visibility = View.VISIBLE
        mChangeVolumeProgress!!.progress = newVolumeProgress
    }

    override fun hideChangeVolume() {
        mChangeVolume!!.visibility = View.GONE
    }

    override fun showChangeBrightness(newBrightnessProgress: Int) {
        mChangeBrightness!!.visibility = View.VISIBLE
        mChangeBrightnessProgress!!.progress = newBrightnessProgress
    }

    override fun hideChangeBrightness() {
        mChangeBrightness!!.visibility = View.GONE
    }

    init {
        init()
    }
}