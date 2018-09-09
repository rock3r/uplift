package me.seebrock3r.elevationtester

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.view.Window

class DialogLayoutParameters private constructor(
    private val context: Context,
    private val height: Int
) {

    fun applyTo(window: Window) {
        if (context.isTablet) {
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, height)
        } else {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height)
        }
    }

    companion object {

        fun fullHeight(activity: Activity): DialogLayoutParameters {
            return DialogLayoutParameters(activity, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        fun wrapHeight(activity: Activity): DialogLayoutParameters {
            return DialogLayoutParameters(activity, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private val Context.isTablet: Boolean
        get() = resources.getBoolean(R.bool.isTablet)
}
