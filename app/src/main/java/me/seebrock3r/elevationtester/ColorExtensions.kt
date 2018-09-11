package me.seebrock3r.elevationtester

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

@ColorInt
fun @receiver:ColorInt Int.setAlphaTo(@IntRange(from = 0, to = 255) alpha: Int): Int =
    Color.argb(alpha, red, green, blue)

// Note: this is NOT thread safe.
private val hsvBuffer = FloatArray(3)

@ColorInt
fun @receiver:ColorInt Int.setBrightnessTo(@FloatRange brightness: Float): Int {
    Color.colorToHSV(this, hsvBuffer)
    hsvBuffer[2] = brightness
    return Color.HSVToColor(alpha, hsvBuffer)
}

val @receiver:ColorInt Int.brightness: Float
    get() {
        Color.colorToHSV(this, hsvBuffer)
        return hsvBuffer[2]
    }
