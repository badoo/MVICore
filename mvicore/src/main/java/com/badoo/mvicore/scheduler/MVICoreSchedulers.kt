package com.badoo.mvicore.scheduler

import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

class MVICoreSchedulers {
    companion object {
        val main = Schedulers.from(Executors.newFixedThreadPool(1))
    }
}
