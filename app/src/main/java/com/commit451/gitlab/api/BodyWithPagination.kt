package com.commit451.gitlab.api

import com.commit451.gitlab.util.LinkHeaderParser

data class BodyWithPagination<T>(
        val body: T,
        val paginationData: LinkHeaderParser.PaginationData
)
