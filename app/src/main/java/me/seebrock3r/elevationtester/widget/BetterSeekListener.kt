package me.seebrock3r.elevationtester.widget

import android.widget.SeekBar

internal interface BetterSeekListener : SeekBar.OnSeekBarChangeListener {

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        // Don't care
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        // Don't care
    }
}
