package com.junkfood.seal.dot.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val downloadDirText: Preference? = findPreference("downloadDir")
        downloadDirText?.summaryProvider = Preference.SummaryProvider<Preference> {
            BaseApplication.downloadDir
        }
    }

}