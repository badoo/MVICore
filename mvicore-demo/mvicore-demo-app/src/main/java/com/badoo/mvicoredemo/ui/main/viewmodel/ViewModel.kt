package com.badoo.mvicoredemo.ui.main.viewmodel

data class ViewModel(
    val buttonColors: List<Int>,
    val counter: Int,
    val imageUrl: String?,
    val imageIsLoading: Boolean
)
