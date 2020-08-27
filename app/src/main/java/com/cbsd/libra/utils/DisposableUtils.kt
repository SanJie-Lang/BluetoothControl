package com.cbsd.libra.utils

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

object DisposableUtils {
    fun createInterval(period: Long, unit: TimeUnit, sub: () -> Unit): Disposable {
        return Observable.interval(period, unit)
            .compose(TransformUtils.defaultSchedulers())
            .subscribe { sub() }
    }
}