package com.commit451.gitlab.api.graphql

import com.apollographql.apollo.api.Error

class ResponseException(val errors: List<Error>): Exception()
