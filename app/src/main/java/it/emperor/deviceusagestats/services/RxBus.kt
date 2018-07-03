package it.emperor.deviceusagestats.services

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor


class RxBus {

    private val bus = PublishProcessor.create<Any>()

    fun send(event: Any) {
        bus.onNext(event)
    }

    fun <T> event(classz: Class<T>): Flowable<T> {
        return bus.ofType(classz)
    }

    fun toObservable(): Flowable<Any> {
        return bus
    }
}