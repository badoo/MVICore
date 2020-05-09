package com.badoo.mvicore.common.feature

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source

interface Feature<in Wish, out State, out News>: Sink<Wish>, Source<State>, Cancellable {
    val news: Source<News>
}
