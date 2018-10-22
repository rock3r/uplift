package me.seebrock3r.elevationtester

import android.graphics.Typeface
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import androidx.annotation.ColorInt
import org.xml.sax.XMLReader

class CodeSpanHandler(private val codeTypeface: Typeface, @ColorInt private val codeColor: Int) : Html.TagHandler {

    override fun handleTag(opening: Boolean, tag: String?, output: Editable, xmlReader: XMLReader?) {
        if (tag.equals("code", ignoreCase = true)) {
            processStrike(opening, output)
        }
    }

    private fun processStrike(opening: Boolean, output: Editable) {
        val outputLength = output.length
        if (opening) {
            output.setSpan(TypefaceSpan(codeTypeface), outputLength, outputLength, Spannable.SPAN_MARK_MARK)
        } else {
            val lastMarkSpan = getLastMarkSpan(output)
            val lastSpanStart = output.getSpanStart(lastMarkSpan)

            output.removeSpan(lastMarkSpan)

            if (lastSpanStart != outputLength) {
                output.setSpan(TypefaceSpan(codeTypeface), lastSpanStart, outputLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                output.setSpan(ForegroundColorSpan(codeColor), lastSpanStart, outputLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun getLastMarkSpan(text: Editable): TypefaceSpan? {
        val spans = text.getSpans(0, text.length, TypefaceSpan::class.java)

        return spans.lastOrNull { text.getSpanFlags(it) == Spanned.SPAN_MARK_MARK }
    }

    private class TypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {

        override fun updateMeasureState(p: TextPaint) {
            p.typeface = typeface
        }

        override fun updateDrawState(tp: TextPaint) {
            tp.typeface = typeface
        }
    }
}
