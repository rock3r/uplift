package me.seebrock3r.elevationtester

import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v7.app.AppCompatActivity
import android.transition.TransitionManager
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main_collapsed.*
import kotlinx.android.synthetic.main.include_controls_collapsed.*
import kotlinx.android.synthetic.main.include_header_collapsed.*

class MainActivity : AppCompatActivity() {

    private lateinit var outlineProvider: TweakableOutlineProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_collapsed)

        outlineProvider = TweakableOutlineProvider(resources = resources, scaleX = 1f, scaleY = 1f, yShift = 0)
        button.outlineProvider = outlineProvider

        setupPanelHeaderControls()
        setupElevationControls()
        setupScaleXYControls()
        setupYShiftControls()

//        yPositionBar.setOnSeekBarChangeListener(object : BetterSeekListener {
//            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                val shift = progress - seekBar.max / 2
//                button.translationY = shift * resources.displayMetrics.density
//            }
//        })


//        yPositionBar.progress = yPositionBar.max / 2
    }

    private var panelExpanded = false

    private fun setupPanelHeaderControls() {
        panelHeader.setOnClickListener {
            if (panelExpanded) collapsePanel() else expandPanel()
            panelExpanded = !panelExpanded
        }
    }

    private fun collapsePanel() {
        TransitionManager.beginDelayedTransition(rootContainer)
        ConstraintSet().apply {
            clone(rootContainer)

            connect(R.id.panelHeader, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            clear(R.id.panelBackground, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        }.applyTo(rootContainer)
        expandCollapseImage.setImageState(intArrayOf(android.R.attr.state_checked), true)
    }

    private fun expandPanel() {
        TransitionManager.beginDelayedTransition(rootContainer)
        ConstraintSet().apply {
            clone(rootContainer)
            clear(R.id.panelHeader, ConstraintSet.TOP)
            connect(R.id.panelHeader, ConstraintSet.BOTTOM, R.id.elevationValue, ConstraintSet.TOP)
        }.applyTo(rootContainer)
        expandCollapseImage.setImageState(intArrayOf(android.R.attr.state_checked), true)
    }

    private fun setupElevationControls() {
        elevationBar.setOnSeekBarChangeListener(
                object : BetterSeekListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        setElevation(progress)
                    }
                }
        )
        elevationValue.text = getString(R.string.elevation_value, 0)
    }

    private fun setElevation(progress: Int) {
        button.elevation = progress * resources.displayMetrics.density
        elevationValue.text = getString(R.string.elevation_value, progress)
    }

    private fun setupScaleXYControls() {
        xScaleBar.setOnSeekBarChangeListener(
                object : BetterSeekListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        setScaleX(progress)
                    }
                }
        )

        yScaleBar.setOnSeekBarChangeListener(
                object : BetterSeekListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        setScaleY(progress)
                    }
                }
        )

        setScaleX(0)
        xScaleValue.setOnClickListener { xScaleBar.progress = xScaleBar.max / 2 }
        yScaleValue.text = getString(R.string.y_scale_value, 0)
        yScaleBar.progress = yScaleBar.max / 2
        xScaleBar.progress = xScaleBar.max / 2
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

    private fun setupYShiftControls() {
        yShiftBar.setOnSeekBarChangeListener(
                object : BetterSeekListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        setShiftY(progress)
                    }
                }
        )
        yShiftValue.text = getString(R.string.y_shift_value, 0)
        yShiftBar.progress = yShiftBar.max / 2
    }

    private fun setShiftY(progress: Int) {
        val shift = progress - yShiftBar.max / 2
        outlineProvider.yShift = shift
        button.invalidateOutline()
        yShiftValue.text = getString(R.string.y_shift_value, shift)
    }

}

