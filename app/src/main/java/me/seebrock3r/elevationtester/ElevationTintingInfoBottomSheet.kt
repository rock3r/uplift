package me.seebrock3r.elevationtester

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.parseAsHtml
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_elevation_tinting_info.*
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import kotlin.coroutines.experimental.CoroutineContext

class ElevationTintingInfoBottomSheet : BottomSheetDialogFragment(), CoroutineScope {

    private var job: Job? = null

    override val coroutineContext: CoroutineContext
        get() = job!! + Dispatchers.Main

    override fun getTheme() = R.style.Theme_Uplift_BottomSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.Theme_Uplift)
        val view = inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.dialog_elevation_tinting_info, container, true)
        view.findViewById<View>(R.id.closeButton).setOnClickListener { dismiss() }
        view.findViewById<View>(R.id.learnMoreButton).setOnClickListener { openLearnMore() }
        return view
    }

    private fun openLearnMore() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://medium.com/@seebrock3r/playing-with-elevation-in-android-again-36b901287249"))
        startActivity(intent)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()

        val blurbHtml = getString(R.string.elevation_tinting_blurb)
        val codeTypeface = ResourcesCompat.getFont(requireContext(), R.font.overpass_mono)!!
        val codeColor = ResourcesCompat.getColor(resources, R.color.colorAccent, requireContext().theme)
        launch {
            val parsedHtml = blurbHtml.parseHtml(codeTypeface, codeColor)
            launch(Dispatchers.Main) {
                tintingInfoBlurb.text = parsedHtml
            }
        }
    }

    private suspend fun String.parseHtml(codeTypeface: Typeface, @ColorInt codeColor: Int): CharSequence =
        withContext(Dispatchers.Default) {
            // Using KTX's String.parseAsHtml
            parseAsHtml(tagHandler = CodeSpanHandler(codeTypeface, codeColor))
        }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        job = null
    }
}
