package com.example.digitaldetox

import android.app.Application
import com.example.digitaldetox.service.MindfulInterventionManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DigitalDetoxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MindfulInterventionManager.init(this)
    }
}
