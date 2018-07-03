package it.emperor.deviceusagestats

import android.app.Application
import android.content.Context
import android.content.res.Resources
import it.emperor.deviceusagestats.di.ServiceModule
import org.codejargon.feather.Feather

class App() : Application() {

    companion object {
        private lateinit var instance: App
        lateinit var feather: Feather

        fun context(): Context {
            return instance.applicationContext
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        feather = Feather.with(ServiceModule())
    }
}