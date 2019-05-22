@file:Suppress("NOTHING_TO_INLINE")
package com.badoo.mvicore

inline fun <T> self(): (T) -> T = { it }
