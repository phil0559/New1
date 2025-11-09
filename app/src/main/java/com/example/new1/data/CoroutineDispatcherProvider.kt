package com.example.new1.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Fournit les [CoroutineDispatcher] utilisés dans l'application afin de faciliter
 * leur substitution lors des tests et de centraliser leur configuration.
 */
interface CoroutineDispatcherProvider {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
}

class DefaultCoroutineDispatcherProvider : CoroutineDispatcherProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val main: CoroutineDispatcher = Dispatchers.Main
}
