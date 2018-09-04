package me.seebrock3r.elevationtester.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.renderscript.Allocation
import androidx.renderscript.RenderScript
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import me.seebrock3r.elevationtester.widget.colorwheel.ScriptC_ColorWheel
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/*
 * This file was adapted from StylingAndroid's repo: https://github.com/StylingAndroid/ColourWheel
 */
class BitmapGenerator(
    private val androidContext: Context,
    private val config: Bitmap.Config,
    private val observer: BitmapObserver
) : ReadWriteProperty<Any, Byte> {

    private val size = Size(0, 0)

    var brightness = Byte.MAX_VALUE

    private var rsCreation: Deferred<RenderScript> = async(CommonPool) {
        RenderScript.create(androidContext).also {
            _renderscript = it
        }
    }

    private var _renderscript: RenderScript? = null
    private val renderscript: RenderScript
        get() {
            assert(rsCreation.isCompleted)
            return _renderscript as RenderScript
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

    override fun getValue(thisRef: Any, property: KProperty<*>): Byte =
        brightness

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Byte) {
        brightness = value
        generate()
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
            generateProcess = launch(CommonPool) {
                rsCreation.await()
                generated.value.also {
                    draw(it)
                    launch(UI) {
                        observer.bitmapChanged(it)
                    }
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
        _renderscript?.destroy()
        rsCreation.takeIf { it.isActive }?.cancel()
    }

    interface BitmapObserver {
        fun bitmapChanged(bitmap: Bitmap)
    }

    private data class Size(var width: Int, var height: Int) {
        val hasDimensions
            get() = width > 0 && height > 0
    }
}
