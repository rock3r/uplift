package me.seebrock3r.elevationtester

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_header.*
import kotlinx.android.synthetic.main.include_panel_controls.*
import me.seebrock3r.elevationtester.widget.BetterSeekListener
import me.seebrock3r.elevationtester.widget.ColorView
import kotlin.math.roundToInt

private const val REQUEST_AMBIENT_COLOR = 7367
private const val REQUEST_SPOT_COLOR = 7368

class MainActivity : AppCompatActivity() {

    private lateinit var outlineProvider: TweakableOutlineProvider

    @Px
    private var buttonVerticalMarginPixel = 0

    private val hitRect = Rect()
    private var panelExpanded = false

    private var dragYOffset = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val cornerRadius = resources.getDimensionPixelSize(R.dimen.control_corner_material).toFloat()
        outlineProvider = TweakableOutlineProvider(cornerRadius = cornerRadius, scaleX = 1f, scaleY = 1f, yShift = 0)
        mainButton.outlineProvider = outlineProvider

        setupPanelHeaderControls()
        setupElevationControls()
        setupScaleXYControls()
        setupYShiftControls()
        setupColorPickersOnAndroidPAndLater()

        setupDragYToMove()

        panelCollapsed()

        val initialButtonElevationDp = resources.getDimensionDpSize(R.dimen.main_button_initial_elevation).roundToInt()
        elevationBar.progress = initialButtonElevationDp
    }

    private fun setupPanelHeaderControls() {
        rootContainer.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun allowsTransition(transition: MotionScene.Transition): Boolean {
                return true
            }

            override fun onTransitionTrigger(view: MotionLayout, triggerId: Int, positive: Boolean, progress: Float) {
                // No-op
            }

            override fun onTransitionStarted(view: MotionLayout, startId: Int, endId: Int) {
                // No-op
                Log.i("!!!!!", "onTransitionStarted")
            }

            override fun onTransitionChange(view: MotionLayout, startState: Int, endState: Int, progress: Float) {
                // No-op
            }

            override fun onTransitionCompleted(view: MotionLayout, state: Int) {
                panelExpanded = state == R.id.constraints_main_expanded
                TransitionManager.beginDelayedTransition(view)
                if (panelExpanded) panelExpanded() else panelCollapsed()
            }
        })
    }

    private fun panelCollapsed() {
        expandCollapseImage.isChecked = false
        mainButton.text = getString(R.string.drag_up_and_down)
    }

    private fun panelExpanded() {
        expandCollapseImage.isChecked = true
        mainButton.text = getString(R.string.use_controls_below)
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
        mainButton.elevation = elevationPixel
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
        mainButton.invalidateOutline()
        xScaleValue.text = getString(R.string.x_scale_value, scale + 100)
    }

    private fun setScaleY(scaleYPercent: Int) {
        val scale = scaleYPercent - yScaleBar.max / 2
        outlineProvider.scaleY = 1 + scale / 100f
        mainButton.invalidateOutline()
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
        mainButton.invalidateOutline()
        yShiftValue.text = getString(R.string.y_shift_value, adjustedShiftYDp)
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun setupColorPickersOnAndroidPAndLater() {
        if (isAndroidPOrLater) {
            ambientColor.setOnClickListener { onColorPickerClicked(ambientColor) }
            spotColor.setOnClickListener { onColorPickerClicked(spotColor) }
            ambientColor.onColorChangedListener = ::onColorChanged
            spotColor.onColorChangedListener = ::onColorChanged

            ambientColor.color = Color.BLACK.setAlphaTo((0.039f * 255).toInt())
            spotColor.color = Color.BLACK.setAlphaTo((0.19f * 255).toInt())
        } else {
            ambientColor.isEnabled = false
            spotColor.isEnabled = false
        }

        infoButton.setOnClickListener { showInfoDialog() }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun onColorPickerClicked(colorView: ColorView) {
        val title = getString(
            if (colorView.id == R.id.ambientColor) R.string.color_picker_title_ambient else R.string.color_picker_title_spot
        )
        val boundsOnScreen = colorView.boundsOnScreen()
        val intent = ColorPickerActivity.createIntent(this, title, colorView.color, boundsOnScreen)

        val requestCode = if (colorView.id == R.id.ambientColor) REQUEST_AMBIENT_COLOR else REQUEST_SPOT_COLOR

        startActivityForResult(intent, requestCode)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun onColorChanged(colorView: ColorView) {
        when (colorView.id) {
            R.id.ambientColor -> mainButton.outlineAmbientShadowColor = colorView.color
            R.id.spotColor -> mainButton.outlineSpotShadowColor = colorView.color
        }
    }

    private fun showInfoDialog() {
        ElevationTintingInfoBottomSheet().show(supportFragmentManager, "elevation-info")
    }

    private fun setupDragYToMove() {
        buttonVerticalMarginPixel = resources.getDimensionPixelSize(R.dimen.main_button_vertical_margin)

        buttonContainer.setOnTouchListener { _, motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> handleDragStart(motionEvent)
                MotionEvent.ACTION_MOVE -> handleDrag(motionEvent)
                MotionEvent.ACTION_UP -> handleDragEnd()
                else -> false
            }
        }
    }

    private fun handleDragStart(motionEvent: MotionEvent): Boolean {
        if (panelExpanded) {
            return false // Only draggable when the panel is collapsed
        }

        mainButton.getHitRect(hitRect)
        dragYOffset = (mainButton.y + mainButton.height / 2F) - motionEvent.y
        val dragOnButton = hitRect.contains(motionEvent.getX(0).roundToInt(), motionEvent.getY(0).roundToInt())

        if (dragOnButton) {
            mainButton.animate()
                .scaleX(1.04F)
                .scaleY(1.04F)
                .duration = resources.getInteger(R.integer.animation_duration_drag_start).toLong()
        }
        return dragOnButton
    }

    private fun handleDrag(motionEvent: MotionEvent): Boolean {
        val minY = buttonContainer.paddingTop.toFloat() + mainButton.height / 2F
        val maxY = buttonContainer.height - buttonContainer.paddingBottom - mainButton.height / 2F
        val availableHeight = maxY - minY

        val coercedY = (motionEvent.y + dragYOffset).coerceIn(minY, maxY)
        val newBias = (coercedY - minY) / availableHeight

        mainButton.layoutParams = (mainButton.layoutParams as ConstraintLayout.LayoutParams)
            .apply { verticalBias = newBias }

        return true
    }

    private fun handleDragEnd(): Boolean {
        mainButton.animate()
            .scaleX(1F)
            .scaleY(1F)
            .duration = resources.getInteger(R.integer.animation_duration_drag_end).toLong()

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data)
        }

        when (requestCode) {
            REQUEST_AMBIENT_COLOR -> ColorPickerActivity.extractResultFrom(data)?.let { selectedColor ->
                ambientColor.color = selectedColor
            }
            REQUEST_SPOT_COLOR -> ColorPickerActivity.extractResultFrom(data)?.let { selectedColor ->
                spotColor.color = selectedColor
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.menu_reset) {
            return super.onOptionsItemSelected(item)
        }
        
        elevationBar.progress = 8
        xScaleBar.progress = xScaleBar.max /2
        yScaleBar.progress = yScaleBar.max /2
        yShiftBar.progress = yShiftBar.max /2
        ambientColor.color = Color.BLACK.setAlphaTo((0.039f * 255).toInt())
        spotColor.color = Color.BLACK.setAlphaTo((0.19f * 255).toInt())
        return true
    }
}

private fun View.boundsOnScreen(): Rect {
    val locationOnScreen = IntArray(2)
    getLocationOnScreen(locationOnScreen)

    return Rect(
        locationOnScreen[0],
        locationOnScreen[1],
        locationOnScreen[0] + width,
        locationOnScreen[1] + height
    )
}

private fun Resources.getDimensionDpSize(@DimenRes dimensionResId: Int): Float =
    getDimensionPixelSize(dimensionResId) / displayMetrics.density
