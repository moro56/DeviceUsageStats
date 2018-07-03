package it.emperor.deviceusagestats.di

import it.emperor.deviceusagestats.services.RxBus
import it.emperor.deviceusagestats.services.UsageService
import org.codejargon.feather.Provides
import javax.inject.Singleton

class ServiceModule {

    @Provides
    @Singleton
    fun usageService(): UsageService {
        return UsageService()
    }

    @Provides
    @Singleton
    fun rxBus(): RxBus {
        return RxBus()
    }
}