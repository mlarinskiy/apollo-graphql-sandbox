package com.github.mlarinskiy.apollo_sandbox.backend.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.github.mlarinskiy.apollo_sandbox.LaunchDetailsQuery
import com.github.mlarinskiy.apollo_sandbox.backend.repository.base.NetworkDataFetcher
import com.github.mlarinskiy.apollo_sandbox.backend.repository.base.ApolloSandboxResponse
import com.github.mlarinskiy.apollo_sandbox.backend.service.ApolloServiceBuilder

interface LaunchDetailsRepository {
    suspend fun getLaunchDetails(id: String): ApolloSandboxResponse<LaunchDetailsQuery.Launch>
}

class LaunchDetailsRepositoryImpl(
    private val apolloClient: ApolloClient = ApolloServiceBuilder.apolloClient
): LaunchDetailsRepository {

    override suspend fun getLaunchDetails(id: String): ApolloSandboxResponse<LaunchDetailsQuery.Launch> {
        val dataFetcher: NetworkDataFetcher<LaunchDetailsQuery.Launch> = object : NetworkDataFetcher<LaunchDetailsQuery.Launch>() {
            override suspend fun fetchDataFromNetwork(): LaunchDetailsQuery.Launch? {
                val apolloClientResponse: Response<LaunchDetailsQuery.Data> =
                    apolloClient
                        .query(LaunchDetailsQuery(id = id))
                        .await()

                return apolloClientResponse.data?.launch
            }
        }

        return dataFetcher.fetchData()
    }

}