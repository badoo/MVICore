package com.badoo.mvicore.android

import android.os.Bundle
import android.os.Parcelable
import com.badoo.mvicore.element.TimeCapsule
import com.badoo.mvicore.feature.BaseFeature

class AndroidTimeCapsule<State : Parcelable>(
    private val feature: BaseFeature<*, *, *, State, *>
) : TimeCapsule<State> {
    private var bundle: Bundle? = null
    private val key = feature::class.java.canonicalName

    fun saveTo(bundle: Bundle) {
        bundle.putParcelable(key, feature.state)
    }

    fun restoreFrom(bundle: Bundle) {
        this.bundle = bundle
        feature.restoreFrom(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun open(): State? =
        bundle?.getParcelable(key) as? State

    companion object {
        fun <T : Parcelable> create(feature: BaseFeature<*, *, *, T, *>): AndroidTimeCapsule<T> =
            AndroidTimeCapsule(feature)
    }
}
