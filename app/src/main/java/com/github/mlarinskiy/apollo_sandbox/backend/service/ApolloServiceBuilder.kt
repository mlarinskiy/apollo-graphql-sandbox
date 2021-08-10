package com.github.mlarinskiy.apollo_sandbox.backend.service

import com.apollographql.apollo.ApolloClient

private const val GRAPHQL_ENDPOINT_URL = "https://apollo-fullstack-tutorial.herokuapp.com/graphql"


object ApolloServiceBuilder {
    val apolloClient: ApolloClient = ApolloClient.builder()
        .serverUrl(GRAPHQL_ENDPOINT_URL)
        .build()
}