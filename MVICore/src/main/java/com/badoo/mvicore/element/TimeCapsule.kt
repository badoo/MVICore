package com.badoo.mvicore.element

interface TimeCapsule<State : Any> {

    fun open(): State?
}
