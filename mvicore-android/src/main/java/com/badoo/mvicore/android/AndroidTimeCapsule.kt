package com.badoo.mvicore.android

import android.os.Bundle
import android.os.Parcelable
import com.badoo.mvicore.element.TimeCapsule
import kotlin.collections.forEach as kotlinForEach

class AndroidTimeCapsule(private val savedState: Bundle?) : TimeCapsule<Parcelable> {

    private val stateSuppliers = hashMapOf<String, () -> Parcelable>()

    override fun <State : Parcelable> get(key: Any): State? =
        savedState?.getParcelable(key.toString())

    override fun <State : Parcelable> register(key: Any, stateSupplier: () -> State) {
        stateSuppliers[key.toString()] = stateSupplier
    }

    fun saveState(outState: Bundle) =
        stateSuppliers.kotlinForEach { entry -> outState.putParcelable(entry.key, entry.value()) }
}
