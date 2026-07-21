package com.germandebustamante.fuelio

import android.app.Application
import android.content.pm.ApplicationInfo
import com.germandebustamante.fuelio.core.logger.AppLogger
import com.germandebustamante.fuelio.di.initKoin
import com.github.anrwatchdog.ANRWatchDog
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class AndroidApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@AndroidApplication)
            androidLogger()
        }

        if (isDebuggable()) {
            ANRWatchDog()
                .setIgnoreDebugger(true)
                .setANRListener { error ->
                    AppLogger.e("ANRWatchDog", "ANR detected", error)
                }
                .start()
        }
    }

    private fun isDebuggable(): Boolean =
        (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

}