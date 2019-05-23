@file:Suppress("NOTHING_TO_INLINE")
package com.badoo.mvicore

inline fun <T> itself(): (T) -> T = { it }
