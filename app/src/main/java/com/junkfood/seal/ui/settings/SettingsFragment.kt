package com.junkfood.seal.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.junkfood.seal.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

}