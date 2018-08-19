package com.badoo.mvicoredemo.utils

interface Logger {
    operator fun invoke(string: String)
}
