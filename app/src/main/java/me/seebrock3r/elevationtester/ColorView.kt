package me.seebrock3r.elevationtester

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import kotlinx.android.synthetic.main.view_color.view.*

class ColorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.colorViewStyle
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var text: String? = null
        set(newText) {
            field = newText
            if (!isInEditMode) colorView_label.text = text
        }

    @ColorInt
    var color: Int = Color.BLACK
        set(newColor) {
            field = newColor
            if (!isInEditMode) onColorChanged()
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.ColorView, defStyleAttr) {
            if (hasValue(R.styleable.ColorView_android_text)) text = getString(R.styleable.ColorView_android_text)
            if (hasValue(R.styleable.ColorView_color)) color = getColor(R.styleable.ColorView_color, color)
        }

        isClickable = true
        isFocusable = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        LayoutInflater.from(context).inflate(R.layout.view_color, this, true)

        if (isInEditMode) {
            onColorChanged()
            colorView_label.text = text
        }
    }

    @SuppressLint("SetTextI18n") // This doesn't require i18n, it's a hex integer representation
    private fun onColorChanged() {
        colorView_color?.backgroundTintList = ColorStateList.valueOf(color)
        colorView_value?.text = String.format("#%08X", 0xFFFFFFFF and color.toLong())
    }
}
