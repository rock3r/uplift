package me.seebrock3r.elevationtester

import android.graphics.Outline
import android.graphics.Rect
import android.os.Bundle
import android.support.annotation.FloatRange
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val outlineProvider = OutlineProvider(scaleX = 1f, scaleY = 1f, yShift = 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.outlineProvider = outlineProvider

        scaleXBar.setOnSeekBarChangeListener(object : BetterSeekListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setScaleX(progress)
            }
        })

        scaleYBar.setOnSeekBarChangeListener(object : BetterSeekListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setScaleY(progress)
            }
        })

        yShiftBar.setOnSeekBarChangeListener(object : BetterSeekListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setShiftY(progress)
            }
        })

        elevationBar.setOnSeekBarChangeListener(object : BetterSeekListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setElevation(progress)
            }
        })

        yPositionBar.setOnSeekBarChangeListener(object : BetterSeekListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val shift = progress - seekBar.max / 2
                button.translationY = shift * resources.displayMetrics.density
            }
        })

        setScaleX(0)
        scaleXLabel.setOnClickListener { scaleXBar.progress = scaleXBar.max / 2 }
        scaleYLabel.text = getString(R.string.scale_y_label, 0)
        scaleYBar.progress = scaleYBar.max / 2
        scaleXBar.progress = scaleXBar.max / 2

        yShiftLabel.text = getString(R.string.y_shift_label, 0)
        yShiftBar.progress = yShiftBar.max / 2

        elevationLabel.text = getString(R.string.elevation_label, 0)
        yPositionBar.progress = yPositionBar.max / 2
    }

    private fun setElevation(progress: Int) {
        button.elevation = progress * resources.displayMetrics.density
        elevationLabel.text = getString(R.string.elevation_label, progress)
    }

    private fun setScaleX(progress: Int) {
        val scale = progress - scaleXBar.max / 2
        outlineProvider.scaleX = 1 + scale / 100f
        button.invalidateOutline()
        scaleXLabel.text = getString(R.string.scale_x_label, scale + 100)
    }

    private fun setScaleY(progress: Int) {
        val scale = progress - scaleYBar.max / 2
        outlineProvider.scaleY = 1 + scale / 100f
        button.invalidateOutline()
        scaleYLabel.text = getString(R.string.scale_y_label, scale + 100)
    }

    private fun setShiftY(progress: Int) {
        val shift = progress - yShiftBar.max / 2
        outlineProvider.yShift = shift
        button.invalidateOutline()
        yShiftLabel.text = getString(R.string.y_shift_label, shift)
    }

    inner class OutlineProvider(private val rect: Rect = Rect(), var scaleX: Float, var scaleY: Float, var yShift: Int) : ViewOutlineProvider() {

        override fun getOutline(view: View?, outline: Outline?) {
            view?.background?.copyBounds(rect)
            rect.scale(scaleX, scaleY)
            rect.offset(0, yShift)
            outline?.setRoundRect(rect, resources.getDimensionPixelSize(R.dimen.control_corner_material).toFloat())
        }
    }
}

private fun Rect.scale(
        @FloatRange(from = -1.0, to = 1.0) scaleX: Float,
        @FloatRange(from = -1.0, to = 1.0) scaleY: Float
) {
    val newWidth = width() * scaleX
    val newHeight = height() * scaleY
    val deltaX = (width() - newWidth) / 2
    val deltaY = (height() - newHeight) / 2

    set((left + deltaX).toInt(), (top + deltaY).toInt(), (right - deltaX).toInt(), (bottom - deltaY).toInt())
}

private interface BetterSeekListener : SeekBar.OnSeekBarChangeListener {
    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        // Don't care
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        // Don't care
    }
}
