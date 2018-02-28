package com.badoo.mvicore.element

import android.support.annotation.MainThread
import io.reactivex.Observable

/**
 * Bootstrappers are used to setup your Features.
 * Here you can subscribe to any data sources or do any other preparation and
 * return Observable of Wishes that will be passed directly to your Feature
 *
 * @param Wish type of Feature's Wishes
 */
interface Bootstrapper<Wish : Any> {
    /**
     * Configures a Feature
     *
     * @return Observable of Wishes
     */
    @MainThread
    operator fun invoke(): Observable<Wish>
}
