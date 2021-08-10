package com.github.mlarinskiy.apollo_sandbox.backend.repository.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatchersProvider {
    fun main(): CoroutineDispatcher
    fun io(): CoroutineDispatcher
}

object DefaultDispatchersProviderImpl: DispatchersProvider {
    override fun main() = Dispatchers.Main
    override fun io() = Dispatchers.IO
}