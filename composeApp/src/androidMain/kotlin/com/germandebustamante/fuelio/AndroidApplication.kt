package com.germandebustamante.fuelio

import android.app.Application
import com.germandebustamante.fuelio.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class AndroidApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@AndroidApplication)
            androidLogger()
        }
    }

}