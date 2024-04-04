package com.badoo.mvicoredemo.ui.main.event

sealed class UiEvent {
    data class ButtonClicked(val idx: Int) : UiEvent()
    data object PlusClicked : UiEvent()
    data object ImageClicked : UiEvent()
}
