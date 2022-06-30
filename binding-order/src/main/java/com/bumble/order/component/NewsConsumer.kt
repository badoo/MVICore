package com.bumble.order.component

import com.badoo.binder.lifecycle.ManualLifecycle
import io.reactivex.functions.Consumer

class NewsConsumer(private val lifecycle: ManualLifecycle) : Consumer<Feature.News> {
    override fun accept(news: Feature.News) {
        when (news) {
            Feature.News.MaxScoreReached -> {
                lifecycle.end()
                println("--FINISH--")
            }
        }
    }
}