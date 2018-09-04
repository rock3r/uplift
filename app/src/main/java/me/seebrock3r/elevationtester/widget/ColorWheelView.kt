package me.seebrock3r.elevationtester.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/*
 * This file was adapted from StylingAndroid's repo: https://github.com/StylingAndroid/ColourWheel
 */
class ColorWheelView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attributeSet, defStyleAttr), BitmapGenerator.BitmapObserver {

    private val bitmapGenerator: BitmapGenerator by lazy(LazyThreadSafetyMode.NONE) {
        BitmapGenerator(context, Bitmap.Config.ARGB_8888, this)
    }

    var brightness: Byte by bitmapGenerator

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w != oldw || h != oldh) {
            bitmapGenerator.setSize(w, h)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bitmapGenerator.brightness = Byte.MAX_VALUE
    }

    override fun bitmapChanged(bitmap: Bitmap) {
        setImageBitmap(bitmap)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bitmapGenerator.stop()
    }
}
