package com.badoo.mvicoredemo.ui.main.event

sealed class UiEvent {
    data class ButtonClicked(val idx: Int) : UiEvent()
    object PlusClicked : UiEvent()
    object ImageClicked : UiEvent()
}
