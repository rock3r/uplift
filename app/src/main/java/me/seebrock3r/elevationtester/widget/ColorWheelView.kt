package me.seebrock3r.elevationtester.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.minus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import me.seebrock3r.elevationtester.R

/*
 * This file was adapted from StylingAndroid's repo: https://github.com/StylingAndroid/ColourWheel
 */
class ColorWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.colorWheelViewStyle
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var job: Job? = null
    private val coroutineContextProducer = { job!! + Dispatchers.Main }

    @Px
    private var widthMinusPadding: Int = 0

    @Px
    private var heightMinusPadding: Int = 0

    @Px
    private var wheelRadius: Float = 0F

    @Px
    private var pickerClickedRadius: Int = 60

    @Px
    private var pickerIdleRadius: Int = 20

    @Px
    private var pickerStrokeWidth: Int = 10

    @Px
    private var pickerSnapThreshold: Int = 20

    private var bitmapGenerator: BitmapGenerator? = null

    private var wheelCenter: PointF? = null
    private var pickerPosition = PointF()

    private var isDragging: Boolean = false

    var onColorChangedListener: ((color: Int) -> Unit)? = null

    private val hsv = FloatArray(3)

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = pickerStrokeWidth.toFloat()
        color = Color.WHITE
        this.style = Paint.Style.STROKE
    }

    @get:ColorInt
    val selectedColor: Int
        get() = Color.HSVToColor(hsv)

    init {
        context.withStyledAttributes(attrs, R.styleable.ColorWheelView, defStyleAttr) {
            setColor(getColor(R.styleable.ColorWheelView_initialColor, Color.BLACK))
            pickerClickedRadius = getDimensionPixelSize(R.styleable.ColorWheelView_pickerClickedRadius, 60)
            pickerIdleRadius = getDimensionPixelSize(R.styleable.ColorWheelView_pickerIdleRadius, 20)
            pickerStrokeWidth = getDimensionPixelSize(R.styleable.ColorWheelView_pickerStrokeWidth, 10)
            pickerSnapThreshold = getDimensionPixelSize(R.styleable.ColorWheelView_pickerSnapThreshold, 20)
        }

        isClickable = true
        isFocusable = true
        outlineProvider = WheelOutlineProvider()
    }

    fun setColor(@ColorInt newColor: Int) {
        Color.colorToHSV(newColor, hsv)
        onColorChangedListener?.invoke(newColor)

        if (isAttachedToWindow) {
            updateWheelAndPicker()
        }
    }

    fun setBrightness(@FloatRange brightness: Float) {
        hsv[2] = brightness
        onColorChangedListener?.invoke(selectedColor)

        if (isAttachedToWindow) {
            updateWheelAndPicker()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        job = Job()
        bitmapGenerator = BitmapGenerator(context, coroutineContextProducer, Bitmap.Config.ARGB_8888, ::setImageBitmap)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w != oldw || h != oldh) {
            onDimensionsChanged()
            bitmapGenerator!!.setSize(widthMinusPadding, heightMinusPadding)
            updateWheelAndPicker()
        }
    }

    private fun updateWheelAndPicker() {
        bitmapGenerator!!.brightness = (hsv[2] * Byte.MAX_VALUE).toByte()

        val hue = Math.toRadians(hsv[0].toDouble())
        val radius = wheelRadius * hsv[1]
        val pickerX = wheelCenter!!.x + (Math.cos(hue) * radius)
        val pickerY = wheelCenter!!.y + (Math.sin(hue) * radius)
        pickerPosition.set(pickerX.toFloat(), pickerY.toFloat())
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        onDimensionsChanged()
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        onDimensionsChanged()
    }

    private fun onDimensionsChanged() {
        widthMinusPadding = width - paddingStart - paddingEnd
        heightMinusPadding = height - paddingTop - paddingBottom

        val minDimension = Math.min(widthMinusPadding, heightMinusPadding)
        wheelRadius = minDimension / 2F
        wheelCenter = PointF(paddingLeft + widthMinusPadding / 2F, paddingTop + heightMinusPadding / 2F)
    }

    @SuppressLint("ClickableViewAccessibility") // TODO: care for a11y
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> isDragging = true
            MotionEvent.ACTION_UP -> isDragging = false
        }
        updatePickerPosition(event)
        onPickerMoved()
        return true
    }

    private fun updatePickerPosition(event: MotionEvent) {
        val center = wheelCenter!!

        val dx: Double = (event.x - center.x).toDouble()
        val dy: Double = (event.y - center.y).toDouble()
        val distanceFromCenter = Math.sqrt(dx * dx + dy * dy)

        when {
            distanceFromCenter > wheelRadius -> {
                // Restrict within wheel diameter
                val theta = Math.atan2(dy, dx)
                pickerPosition.x = (center.x + wheelRadius * Math.cos(theta)).toFloat()
                pickerPosition.y = (center.y + wheelRadius * Math.sin(theta)).toFloat()
            }
            distanceFromCenter < pickerSnapThreshold -> {
                // Snap to center when close enough
                pickerPosition.set(center.x, center.y)
            }
            else -> pickerPosition.set(event.x, event.y)
        }
    }

    private fun onPickerMoved() {
        val center = wheelCenter!!

        val dx: Double = (pickerPosition.x - center.x).toDouble()
        val dy: Double = (pickerPosition.y - center.y).toDouble()

        var hue = Math.toDegrees(Math.atan2(dy, dx)).toFloat()
        if (hue < 0F) hue += 360F
        hsv[0] = hue

        val distanceFromCenter = Math.sqrt(dx * dx + dy * dy)
        hsv[1] = Math.abs(distanceFromCenter / wheelRadius).toFloat()
        invalidate()

        onColorChangedListener?.invoke(selectedColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isDragging) {
            fillPaint.color = selectedColor
            val radius = pickerClickedRadius.toFloat()
            canvas.drawCircle(pickerPosition.x, pickerPosition.y, radius, fillPaint)
            canvas.drawPickerBorder(radius)
        } else {
            canvas.drawPickerBorder(pickerIdleRadius.toFloat())
        }
    }

    private fun Canvas.drawPickerBorder(radius: Float) {
        strokePaint.apply {
            color = Color.WHITE
            strokeWidth = pickerStrokeWidth.toFloat()
        }
        drawCircle(pickerPosition.x, pickerPosition.y, radius, strokePaint)

        strokePaint.apply {
            color = Color.BLACK
            strokeWidth = pickerStrokeWidth / 2f
        }
        drawCircle(pickerPosition.x, pickerPosition.y, radius - pickerStrokeWidth / 2, strokePaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job?.cancel()
        job = null
        bitmapGenerator?.stop()
        bitmapGenerator = null
    }

    private class WheelOutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            (view as ColorWheelView).apply {
                wheelCenter?.minus(wheelRadius)?.let { wheelPosition: PointF ->
                    val wheelDiameter: Int = (wheelRadius * 2).toInt()
                    outline.setOval(
                        wheelPosition.x.toInt(),
                        wheelPosition.y.toInt(),
                        wheelPosition.x.toInt() + wheelDiameter,
                        wheelPosition.y.toInt() + wheelDiameter
                    )
                }
            }
        }
    }
}
