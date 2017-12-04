package me.seebrock3r.elevationtester

import android.content.res.Resources
import android.graphics.Outline
import android.graphics.Rect
import android.support.annotation.FloatRange
import android.view.View
import android.view.ViewOutlineProvider

internal class TweakableOutlineProvider(val resources: Resources, var scaleX: Float, var scaleY: Float, var yShift: Int) : ViewOutlineProvider() {

    private val rect: Rect = Rect()

    override fun getOutline(view: View?, outline: Outline?) {
        view?.background?.copyBounds(rect)
        rect.scale(scaleX, scaleY)
        rect.offset(0, yShift)
        outline?.setRoundRect(rect, resources.getDimensionPixelSize(R.dimen.control_corner_material).toFloat())
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
