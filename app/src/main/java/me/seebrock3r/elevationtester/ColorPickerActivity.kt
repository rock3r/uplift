package me.seebrock3r.elevationtester

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_color_picker.*
import me.seebrock3r.elevationtester.widget.BetterSeekListener

class ColorPickerActivity : AppCompatActivity() {

    private var changingBrightnessFromCode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_picker)

        DialogLayoutParameters.wrapHeight(this)
            .applyTo(window)

        dialogColorAlpha.setOnSeekBarChangeListener(
            object : BetterSeekListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    dialogColorWheel.color = dialogColorWheel.color.setAlphaTo(progress)
                    val alpha = progress / dialogColorAlpha.max.toFloat()
                    dialogAlphaValue.text = "%.2f".format(alpha)
                }
            }
        )
        dialogColorAlpha.progress = dialogColorAlpha.max

        dialogColorBrightness.setOnSeekBarChangeListener(
            object : BetterSeekListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (changingBrightnessFromCode) {
                        return
                    }

                    val brightness = progress / dialogColorBrightness.max.toFloat()
                    dialogColorWheel.color = dialogColorWheel.color.setBrightnessTo(brightness)
                    dialogBrightnessValue.text = "%.2f".format(brightness)
                }
            }
        )
        dialogColorBrightness.progress = dialogColorBrightness.max

        dialogColorWheel.onColorChangedListener = {
            changingBrightnessFromCode = true
            dialogColorBrightness.progress = (it.brightness * dialogColorBrightness.max).toInt()
            changingBrightnessFromCode = false
        }
    }
}
