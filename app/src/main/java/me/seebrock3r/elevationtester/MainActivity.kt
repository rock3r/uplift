package me.seebrock3r.elevationtester

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.transition.TransitionManager
import android.view.MotionEvent
import android.widget.SeekBar
import androidx.annotation.DimenRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.activity_main_collapsed.*
import kotlinx.android.synthetic.main.include_controls_collapsed.*
import kotlinx.android.synthetic.main.include_header_collapsed.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var outlineProvider: TweakableOutlineProvider
    private var buttonVerticalMarginPixel = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_collapsed)

        outlineProvider = TweakableOutlineProvider(resources = resources, scaleX = 1f, scaleY = 1f, yShift = 0)
        main_button.outlineProvider = outlineProvider

        setupPanelHeaderControls()
        setupElevationControls()
        setupScaleXYControls()
        setupYShiftControls()

        setupDragYToMove()

        collapsePanel(animate = false)

        val initialButtonElevationDp = resources.getDimensionDpSize(R.dimen.main_button_initial_elevation).roundToInt()
        elevationBar.progress = initialButtonElevationDp
    }

    private var panelExpanded = false

    private fun setupPanelHeaderControls() {
        panelHeader.setOnClickListener {
            if (panelExpanded) collapsePanel() else expandPanel()
            panelExpanded = !panelExpanded
        }
    }

    private fun collapsePanel(animate: Boolean = true) {
        if (animate) {
            TransitionManager.beginDelayedTransition(rootContainer)
        }

        ConstraintSet().apply {
            clone(this@MainActivity, R.layout.activity_main_collapsed)
        }.applyTo(rootContainer)
        expandCollapseImage.isChecked = false
        main_button.text = getString(R.string.drag_up_and_down)
    }

    private fun expandPanel() {
        TransitionManager.beginDelayedTransition(rootContainer)
        ConstraintSet().apply {
            clone(this@MainActivity, R.layout.activity_main_expanded)
        }.applyTo(rootContainer)
        expandCollapseImage.isChecked = true
        main_button.text = getString(R.string.use_controls_below)
    }

    private fun setupElevationControls() {
        elevationBar.setOnSeekBarChangeListener(
            object : BetterSeekListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    setElevationDp(progress)
                }
            }
        )
        elevationValue.text = getString(R.string.elevation_value, 0)
    }

    private fun setElevationDp(elevationDp: Int) {
        val elevationPixel = elevationDp * resources.displayMetrics.density
        main_button.elevation = elevationPixel
        elevationValue.text = getString(R.string.elevation_value, elevationDp)
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

    private fun setScaleX(scaleXPercent: Int) {
        val scale = scaleXPercent - xScaleBar.max / 2
        outlineProvider.scaleX = 1 + scale / 100f
        main_button.invalidateOutline()
        xScaleValue.text = getString(R.string.x_scale_value, scale + 100)
    }

    private fun setScaleY(scaleYPercent: Int) {
        val scale = scaleYPercent - yScaleBar.max / 2
        outlineProvider.scaleY = 1 + scale / 100f
        main_button.invalidateOutline()
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

    private fun setShiftY(shiftYDp: Int) {
        val adjustedShiftYDp = shiftYDp - yShiftBar.max / 2
        val adjustedShiftYPixel = adjustedShiftYDp * resources.displayMetrics.density
        outlineProvider.yShift = adjustedShiftYPixel.roundToInt()
        main_button.invalidateOutline()
        yShiftValue.text = getString(R.string.y_shift_value, adjustedShiftYDp)
    }

    private fun setupDragYToMove() {
        buttonVerticalMarginPixel = resources.getDimensionPixelSize(R.dimen.main_button_vertical_margin)

        rootContainer.setOnTouchListener { _, motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> handleActionDown(motionEvent)
                MotionEvent.ACTION_MOVE -> handleDrag(motionEvent)
                else -> false
            }
        }
    }

    private fun handleActionDown(motionEvent: MotionEvent): Boolean {
        if (panelExpanded) {
            return false    // Only draggable when the panel is collapsed
        }

        val hitRect = Rect()
        main_button.getHitRect(hitRect)
        return hitRect.contains(motionEvent.getX(0).roundToInt(), motionEvent.getY(0).roundToInt())
    }

    private fun handleDrag(motionEvent: MotionEvent): Boolean {
        val availableHeight = panelHeader.y
        val clampedEventY = motionEvent.getY(0)
            .roundToInt()
            .coerceIn(buttonVerticalMarginPixel, availableHeight.toInt() - buttonVerticalMarginPixel)

        val layoutParams = main_button.layoutParams as ConstraintLayout.LayoutParams
        val minimumBias = buttonVerticalMarginPixel / availableHeight
        val maximumBias = (availableHeight - main_button.height) / availableHeight

        layoutParams.verticalBias = ((clampedEventY - main_button.height / 2) / availableHeight)
            .coerceIn(minimumBias, maximumBias)
        main_button.layoutParams = layoutParams
        return true
    }
}

private fun Resources.getDimensionDpSize(@DimenRes dimensionResId: Int): Float = getDimensionPixelSize(dimensionResId) / displayMetrics.density
