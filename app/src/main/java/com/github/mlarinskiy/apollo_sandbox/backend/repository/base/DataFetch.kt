package com.github.mlarinskiy.apollo_sandbox.backend.repository.base

import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.UnsupportedOperationException

enum class DataFetchStyle {
    NETWORK_ONLY,
    LOCAL_ONLY,
    NETWORK_FIRST_FALLBACK_LOCAL,
    LOCAL_FIRST_REFRESH_NETWORK;

    companion object {
        val DEFAULT = NETWORK_ONLY
    }
}

sealed class DataFetchResult {
    object Success: DataFetchResult()
    data class Failure(
        val reason: Reason
    ): DataFetchResult() {
        enum class Reason {
            NETWORK_ERROR,
            API_ERROR,
            LOCAL_ERROR
        }
    }
}

abstract class DataFetcher<T>(
    private val dataFetchStyle: DataFetchStyle = DataFetchStyle.DEFAULT
) {

    protected abstract suspend fun fetchDataFromLocal(): T?
    protected abstract suspend fun fetchDataFromNetwork(): T?

    protected open suspend fun <TRANSPORT> mapTransportToDomain(transportData: TRANSPORT?): T? {
        try {
            return transportData as? T
        } catch (e: Exception) {
            throw ClassCastException(
                "$e - Cannot convert ${transportData!!::class.java.simpleName} to domain model type, " +
                        "override this method to provide conversion."
            )
        }
    }


    suspend fun fetchData(
        dispatchersProvider: DispatchersProvider = DefaultDispatchersProviderImpl
    ): ApolloSandboxResponse<T> = withContext(dispatchersProvider.io()) { fetchDataByStyle() }

    private suspend fun fetchDataByStyle(): ApolloSandboxResponse<T> {
        return when (dataFetchStyle) {
            DataFetchStyle.NETWORK_ONLY -> tryFetchDataFromNetwork()
            DataFetchStyle.LOCAL_ONLY -> tryFetchDataFromLocal()
            DataFetchStyle.NETWORK_FIRST_FALLBACK_LOCAL -> TODO()
            DataFetchStyle.LOCAL_FIRST_REFRESH_NETWORK -> TODO()
        }
    }

    private suspend fun tryFetchDataFromNetwork(): ApolloSandboxResponse<T> {
        var result: T? = null
        var error: Throwable? = null
        var dataFetchResult: DataFetchResult

        try {
            result = fetchDataFromNetwork()
            dataFetchResult = DataFetchResult.Success
        } catch (e: Throwable) {
            error = e
            dataFetchResult = DataFetchResult.Failure(
                reason = when (e) {
                    is IOException -> DataFetchResult.Failure.Reason.NETWORK_ERROR
                    else -> DataFetchResult.Failure.Reason.API_ERROR
                }
            )
        }

        return ApolloSandboxResponse(result, dataFetchStyle, dataFetchResult, error)
    }

    private suspend fun tryFetchDataFromLocal(): ApolloSandboxResponse<T> {
        var result: T? = null
        var error: Throwable? = null
        var dataFetchResult: DataFetchResult

        try {
            result = fetchDataFromLocal()
            dataFetchResult = DataFetchResult.Success
        } catch (e: Throwable) {
            error = e
            dataFetchResult = DataFetchResult.Failure(
               reason = DataFetchResult.Failure.Reason.LOCAL_ERROR
            )
        }

        return ApolloSandboxResponse(result, dataFetchStyle, dataFetchResult, error)
    }
}

abstract class NetworkDataFetcher<T>: DataFetcher<T>(dataFetchStyle = DataFetchStyle.NETWORK_ONLY) {
    override suspend fun fetchDataFromLocal(): T? {
        throw UnsupportedOperationException(
            "${this::class.java.simpleName} supports network data fetching only."
        )
    }
}