package com.chooloo.www.koler

import android.content.Context
import android.telecom.TelecomManager
import androidx.preference.PreferenceManager
import com.chooloo.www.chooloolib.ChoolooApp
import com.chooloo.www.chooloolib.notification.CallNotification
import dagger.hilt.android.HiltAndroidApp
import top.niunaijun.blackbox.BlackBoxCore
import javax.inject.Inject

@HiltAndroidApp
open class KolerApp : ChoolooApp() {
    @Inject lateinit var telecomManager: TelecomManager
    @Inject lateinit var callNotification: CallNotification

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        try {
            BlackBoxCore.get().doAttachBaseContext(base, object : top.niunaijun.blackbox.app.configuration.ClientConfiguration() {
                override fun getHostPackageName(): String {
                    return base.packageName
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate() {
        super.onCreate()
        PreferenceManager.setDefaultValues(this, R.xml.preferences_koler, false)
        
        try {
            BlackBoxCore.get().doCreate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}