package me.seebrock3r.elevationtester.widget

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import me.seebrock3r.elevationtester.widget.colorwheel.ScriptC_ColorWheel
import kotlin.coroutines.experimental.CoroutineContext

/*
 * This file was adapted from StylingAndroid's repo: https://github.com/StylingAndroid/ColourWheel
 */
class BitmapGenerator(
    private val androidContext: Context,
    private val coroutineUiContextProducer: () -> CoroutineContext,
    private val config: Bitmap.Config,
    private val observer: (bitmap: Bitmap) -> Unit
) {

    private val size = Size(0, 0)

    var brightness = Byte.MAX_VALUE
        set(value) {
            field = value
            generate()
        }

    private var rsCreation: Deferred<RenderScript> = GlobalScope.async(Dispatchers.Default) {
        RenderScript.create(androidContext).also { renderscript = it }
    }

    private var renderscript: RenderScript? = null
        get() {
            require(rsCreation.isCompleted)
            return field as RenderScript
        }

    private var generateProcess: Job? = null

    private val generated = AutoCreate(Bitmap::recycle) {
        Bitmap.createBitmap(size.width, size.height, config)
    }

    private val generatedAllocation = AutoCreate(Allocation::destroy) {
        Allocation.createFromBitmap(
            renderscript,
            generated.value,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )
    }

    private val colourWheelScript = AutoCreate(ScriptC_ColorWheel::destroy) {
        ScriptC_ColorWheel(renderscript)
    }

    fun setSize(width: Int, height: Int) {
        size.takeIf { it.width != width || it.height != height }?.also {
            generated.clear()
            generatedAllocation.clear()
        }
        size.width = width
        size.height = height
        generate()
    }

    private fun generate() {
        if (size.hasDimensions && generateProcess?.isCompleted != false) {
            generateProcess = GlobalScope.async(Dispatchers.Default) {
                rsCreation.await()
                generated.value.also {
                    draw(it)
                    launch(coroutineUiContextProducer()) { observer(it) }
                }
            }
        }
    }

    private fun draw(bitmap: Bitmap) {
        generatedAllocation.value.apply {
            copyFrom(bitmap)
            colourWheelScript.value.invoke_colorWheel(
                colourWheelScript.value,
                this,
                brightness.toFloat() / Byte.MAX_VALUE.toFloat()
            )
            copyTo(bitmap)
        }
    }

    fun stop() {
        generated.clear()
        generatedAllocation.clear()
        colourWheelScript.clear()
        renderscript?.destroy()
        rsCreation.takeIf { it.isActive }?.cancel()
    }

    private data class Size(var width: Int, var height: Int) {
        val hasDimensions
            get() = width > 0 && height > 0
    }
}
