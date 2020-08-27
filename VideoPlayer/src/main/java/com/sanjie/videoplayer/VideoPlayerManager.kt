package com.sanjie.videoplayer

/**
 * Created by SanJie on 2019.04.16
 * 视频播放器管理器.
 */
class VideoPlayerManager private constructor() {
    var currentVideoPlayer: VideoPlayer? = null

    fun setVideoPlayer(videoPlayer: VideoPlayer) {
        if(currentVideoPlayer != videoPlayer) {
            releaseVideoPlayer()
            this.currentVideoPlayer = videoPlayer
        }
    }

    fun suspendVideoPlayer() {
        if (currentVideoPlayer != null && (currentVideoPlayer!!.isPlaying || currentVideoPlayer!!.isBufferingPlaying)) {
            currentVideoPlayer!!.pause()
        }
    }

    fun resumeVideoPlayer() {
        if (currentVideoPlayer != null && (currentVideoPlayer!!.isPaused || currentVideoPlayer!!.isBufferingPaused)) {
            currentVideoPlayer!!.restart()
        }
    }

    fun releaseVideoPlayer() {
        if (currentVideoPlayer != null) {
            currentVideoPlayer!!.release()
            currentVideoPlayer = null
        }
    }

    fun onBackPressed(): Boolean {
        if (currentVideoPlayer != null) {
            if (currentVideoPlayer!!.isFullScreen) {
                return currentVideoPlayer!!.exitFullScreen()
            } else if (currentVideoPlayer!!.isTinyWindow) {
                return currentVideoPlayer!!.exitTinyWindow()
            }
        }
        return false
    }

    companion object {
        private var sInstance: VideoPlayerManager? = null

        @Synchronized
        fun instance(): VideoPlayerManager? {
            if (sInstance == null) {
                sInstance = VideoPlayerManager()
            }
            return sInstance
        }
    }
}