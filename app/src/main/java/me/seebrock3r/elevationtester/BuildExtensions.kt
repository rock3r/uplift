package me.seebrock3r.elevationtester

import android.os.Build

internal val isAndroidPOrLater
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
