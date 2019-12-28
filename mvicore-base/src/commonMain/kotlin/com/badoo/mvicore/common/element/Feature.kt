package com.badoo.mvicore.common.element

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source

interface Feature<Wish, State, News>: Sink<Wish>, Source<State>, Cancellable {
    val news: Source<News>
}
