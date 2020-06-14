package com.commit451.gitlab.api.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.rx3.rx
import io.reactivex.rxjava3.annotations.CheckReturnValue
import io.reactivex.rxjava3.core.Observable

/**
 * Creates a new [ApolloQueryCall] call and then converts it to an [Observable], mapping errors that
 * might occur to onError
 *
 * The number of emissions this Observable will have is based on the
 * [com.apollographql.apollo.fetcher.ResponseFetcher] used with the call.
 */
@JvmSynthetic
@CheckReturnValue
inline fun <D : Operation.Data, T, V : Operation.Variables> ApolloClient.rxQueryMapErrors(
        query: Query<D, T, V>,
        configure: ApolloQueryCall<T>.() -> ApolloQueryCall<T> = { this }
): Observable<T> = query(query).configure().rx().map {
    val errors = it.errors
    if (errors != null) {
        throw ResponseException(errors)
    } else {
        it.data
    }
}
