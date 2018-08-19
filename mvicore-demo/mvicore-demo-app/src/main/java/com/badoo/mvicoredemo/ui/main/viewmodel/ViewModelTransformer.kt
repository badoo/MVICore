package com.badoo.mvicoredemo.ui.main.viewmodel

import com.badoo.feature1.Feature1
import com.badoo.feature2.Feature2
import com.badoo.mvicoredemo.R

class ViewModelTransformer : (Pair<Feature1.State, Feature2.State>) -> ViewModel {

    override fun invoke(pair: Pair<Feature1.State, Feature2.State>): ViewModel {
        val (state1, state2) = pair

        return ViewModel(
            buttonColors = colors(state1.activeButtonIdx),
            counter = state1.counter,
            imageIsLoading = state2.isLoading,
            imageUrl = state2.imageUrl
        )
    }

    private fun colors(active: Int?): List<Int> = listOf(
        if (active == 0) R.color.pink_800 else R.color.pink_500,
        if (active == 1) R.color.light_blue_800 else R.color.light_blue_500,
        if (active == 2) R.color.lime_800 else R.color.lime_500,
        if (active == 3) R.color.yellow_800 else R.color.yellow_500
    )
}
