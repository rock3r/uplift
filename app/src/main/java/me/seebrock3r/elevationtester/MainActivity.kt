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

        xScaleBar.setOnSeekBarChangeListener(object : BetterSeekListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setScaleX(progress)
            }
        })

        yScaleBar.setOnSeekBarChangeListener(object : BetterSeekListener {
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

//        yPositionBar.setOnSeekBarChangeListener(object : BetterSeekListener {
//            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                val shift = progress - seekBar.max / 2
//                button.translationY = shift * resources.displayMetrics.density
//            }
//        })

        setScaleX(0)
        xScaleValue.setOnClickListener { xScaleBar.progress = xScaleBar.max / 2 }
        yScaleValue.text = getString(R.string.y_scale_value, 0)
        yScaleBar.progress = yScaleBar.max / 2
        xScaleBar.progress = xScaleBar.max / 2

        yShiftValue.text = getString(R.string.y_shift_value, 0)
        yShiftBar.progress = yShiftBar.max / 2

        elevationValue.text = getString(R.string.elevation_value, 0)
//        yPositionBar.progress = yPositionBar.max / 2
    }

    private fun setElevation(progress: Int) {
        button.elevation = progress * resources.displayMetrics.density
        elevationValue.text = getString(R.string.elevation_value, progress)
    }

    private fun setScaleX(progress: Int) {
        val scale = progress - xScaleBar.max / 2
        outlineProvider.scaleX = 1 + scale / 100f
        button.invalidateOutline()
        xScaleValue.text = getString(R.string.x_scale_value, scale + 100)
    }

    private fun setScaleY(progress: Int) {
        val scale = progress - yScaleBar.max / 2
        outlineProvider.scaleY = 1 + scale / 100f
        button.invalidateOutline()
        yScaleValue.text = getString(R.string.y_scale_value, scale + 100)
    }

    private fun setShiftY(progress: Int) {
        val shift = progress - yShiftBar.max / 2
        outlineProvider.yShift = shift
        button.invalidateOutline()
        yShiftValue.text = getString(R.string.y_shift_value, shift)
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
