package com.cbsd.libra.listener

import android.widget.SeekBar

interface TSeekBarChangeListener: SeekBar.OnSeekBarChangeListener {

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {

    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
    }
}