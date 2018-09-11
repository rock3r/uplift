package me.seebrock3r.elevationtester.widget

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
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.minus
import me.seebrock3r.elevationtester.R
import me.seebrock3r.elevationtester.brightness

/*
 * This file was adapted from StylingAndroid's repo: https://github.com/StylingAndroid/ColourWheel
 */
class ColorWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.colorWheelViewStyle
) : AppCompatImageView(context, attrs, defStyleAttr), BitmapGenerator.BitmapObserver {

    private val bitmapGenerator = BitmapGenerator(context, Bitmap.Config.ARGB_8888, this)

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

    private var wheelCenter: PointF? = null
    private var indicatorCenter: PointF? = null

    private var isDragging: Boolean = false

    var onColorChangedListener: ((color: Int) -> Unit)? = null

    var color: Int = Color.BLACK
        set(value) {
            field = value
            bitmapGenerator.brightness = (color.brightness * Byte.MAX_VALUE).toByte()
            onColorChangedListener?.invoke(value)
        }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = pickerStrokeWidth.toFloat()
        color = Color.WHITE
        this.style = Paint.Style.STROKE
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.ColorWheelView, defStyleAttr) {
            color = getColor(R.styleable.ColorWheelView_initialColor, Color.BLACK)
            pickerClickedRadius = getDimensionPixelSize(R.styleable.ColorWheelView_pickerClickedRadius, 60)
            pickerIdleRadius = getDimensionPixelSize(R.styleable.ColorWheelView_pickerIdleRadius, 20)
            pickerStrokeWidth = getDimensionPixelSize(R.styleable.ColorWheelView_pickerStrokeWidth, 10)
            pickerSnapThreshold = getDimensionPixelSize(R.styleable.ColorWheelView_pickerSnapThreshold, 20)
        }

        isClickable = true
        isFocusable = true
        outlineProvider = WheelOutlineProvider()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w != oldw || h != oldh) {
            onDimensionsChanged()
            bitmapGenerator.setSize(widthMinusPadding, heightMinusPadding)
        }
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

    override fun bitmapChanged(bitmap: Bitmap) {
        setImageBitmap(bitmap)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> isDragging = true
            MotionEvent.ACTION_UP -> isDragging = false
        }
        indicatorCenter = event.calculateIndicatorPosition()
        onIndicatorMoved()
        return true
    }

    private fun onIndicatorMoved() {
        val center = wheelCenter!!
        val indicator = indicatorCenter!!

        val dx: Double = (indicator.x - center.x).toDouble()
        val dy: Double = (indicator.y - center.y).toDouble()
        val hsv = FloatArray(3)

        var hue = Math.toDegrees(Math.atan2(dy, dx)).toFloat()
        if (hue < 0F) hue += 360F
        hsv[0] = hue

        val distanceFromCenter = Math.sqrt(dx * dx + dy * dy)
        hsv[1] = Math.abs(distanceFromCenter / wheelRadius).toFloat()
        hsv[2] = color.brightness

        color = Color.HSVToColor(hsv)
    }

    private fun MotionEvent.calculateIndicatorPosition(): PointF {
        val center = wheelCenter!!

        val dx: Double = (x - center.x).toDouble()
        val dy: Double = (y - center.y).toDouble()
        val distanceFromCenter = Math.sqrt(dx * dx + dy * dy)
        val indicatorPoint = PointF(x, y)

        when {
            distanceFromCenter > wheelRadius -> {
                // Restrict within wheel diameter
                val theta = Math.atan2(dy, dx)
                indicatorPoint.x = (center.x + wheelRadius * Math.cos(theta)).toFloat()
                indicatorPoint.y = (center.y + wheelRadius * Math.sin(theta)).toFloat()
            }
            distanceFromCenter < pickerSnapThreshold -> {
                // Snap to center when close enough
                indicatorPoint.x = center.x
                indicatorPoint.y = center.y
            }
        }

        return indicatorPoint
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        indicatorCenter?.let { indicator ->
            if (isDragging) {
                fillPaint.color = color
                val radius = pickerClickedRadius.toFloat()
                canvas.drawCircle(indicator.x, indicator.y, radius, fillPaint)
                canvas.drawIndicator(indicator, radius)
            } else if (indicatorCenter != null) {
                canvas.drawIndicator(indicator, pickerIdleRadius.toFloat())
            }
        }
    }

    private fun Canvas.drawIndicator(indicator: PointF, radius: Float) {
        strokePaint.apply {
            color = Color.WHITE
            strokeWidth = pickerStrokeWidth.toFloat()
        }
        drawCircle(indicator.x, indicator.y, radius, strokePaint)

        strokePaint.apply {
            color = Color.BLACK
            strokeWidth = pickerStrokeWidth / 2f
        }
        drawCircle(indicator.x, indicator.y, radius - pickerStrokeWidth / 2, strokePaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bitmapGenerator.stop()
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
