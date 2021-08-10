package com.github.mlarinskiy.apollo_sandbox.backend.repository.base

data class ApolloSandboxResponse<T>(
    val data: T?,
    val dataFetchStyle: DataFetchStyle,
    val dataFetchResult: DataFetchResult,

    val error: Throwable? = null,
)