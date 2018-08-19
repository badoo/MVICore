package com.badoo.mvicoredemo.ui.main.event

import com.badoo.feature1.Feature1
import com.badoo.feature1.Feature1.Wish.*
import com.badoo.mvicoredemo.ui.main.event.UiEvent.ButtonClicked
import com.badoo.mvicoredemo.ui.main.event.UiEvent.ImageClicked
import com.badoo.mvicoredemo.ui.main.event.UiEvent.PlusClicked

class UiEventTransformer1 : (UiEvent) -> Feature1.Wish? {
    override fun invoke(event: UiEvent): Feature1.Wish? = when (event) {
        is ButtonClicked -> Feature1.Wish.SetActiveButton(event.idx)
        is PlusClicked -> Feature1.Wish.IncreaseCounter
        is ImageClicked -> null
    }
}
