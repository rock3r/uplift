package me.seebrock3r.elevationtester

import android.content.Context
import android.preference.PreferenceManager

class OnboardingPreferences(context: Context) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    val shouldShowOnboarding: Boolean
        get() = !preferences.getBoolean(KEY_ONBOARDING_SEEN, false)

    fun storeOnboardingShown() {
        preferences.edit()
            .putBoolean(KEY_ONBOARDING_SEEN, true)
            .apply()
    }

    companion object {
        private const val KEY_ONBOARDING_SEEN = "onboarding_seen"
    }
}
