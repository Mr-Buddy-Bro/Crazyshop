package com.tech2develop.crazyshop

import android.app.Application
import com.onesignal.OneSignal

const val ONESIGNAL_APP_ID = "57ad831f-e2a9-4869-9439-33fbd1951269"

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
    }
}