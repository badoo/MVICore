package com.badoo.mvicoredemo.ui.main.event

import com.badoo.feature2.Feature2
import com.badoo.feature2.Feature2.Wish.*
import com.badoo.mvicoredemo.ui.main.event.UiEvent.ButtonClicked
import com.badoo.mvicoredemo.ui.main.event.UiEvent.ImageClicked
import com.badoo.mvicoredemo.ui.main.event.UiEvent.PlusClicked

class UiEventTransformer2 : (UiEvent) -> Feature2.Wish? {
    override fun invoke(event: UiEvent): Feature2.Wish? = when (event) {
        is ButtonClicked -> null
        is PlusClicked -> null
        is ImageClicked -> LoadNewImage
    }
}
