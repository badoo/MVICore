package com.badoo.mvicoredemo.ui.main.news

import android.content.Context
import android.widget.Toast
import com.badoo.feature2.Feature2
import com.badoo.feature2.Feature2.News.ErrorExecutingRequest
import io.reactivex.rxjava3.functions.Consumer
import timber.log.Timber

class NewsListener(
    private val context: Context
) : Consumer<Feature2.News> {

    override fun accept(news: Feature2.News) {
        when (news) {
            is ErrorExecutingRequest -> errorHappened(news.throwable)
        }
    }

    fun errorHappened(throwable: Throwable) {
        Toast.makeText(context, "Simulated error was triggered", Toast.LENGTH_SHORT).show()
        Timber.w(throwable)
    }
}
