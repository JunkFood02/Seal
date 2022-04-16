package com.junkfood.seal

import android.app.Application
import android.content.res.Resources
import com.google.android.material.color.DynamicColors

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        res = resources
        DynamicColors.applyToActivitiesIfAvailable(this);
    }

    companion object {
        lateinit var res: Resources
    }
}