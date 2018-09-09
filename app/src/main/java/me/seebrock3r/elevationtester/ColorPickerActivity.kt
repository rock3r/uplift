package me.seebrock3r.elevationtester

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ColorPickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_picker)

        DialogLayoutParameters.wrapHeight(this)
            .applyTo(window)
    }
}
