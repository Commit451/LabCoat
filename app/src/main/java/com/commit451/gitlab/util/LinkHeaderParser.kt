package com.commit451.gitlab.util

import retrofit2.Response
import timber.log.Timber

/**
 * Parses link headers from a retrofit [Response]
 * @see [https://www.w3.org/wiki/LinkHeader](https://www.w3.org/wiki/LinkHeader)
 */
object LinkHeaderParser {

    private const val NEXT_PAGE_SUFFIX = "rel=\"next\""

    fun parse(response: Response<*>): PaginationData {
        var next: String? = null

        val header = response.headers()["Link"]
        if (header != null) {
            val nextPagePart = header.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    .find { it.contains(NEXT_PAGE_SUFFIX) } ?: ""
            try {
                val linkStart = nextPagePart.indexOf('<') + 1
                val linkEnd = nextPagePart.indexOf('>')

                next = nextPagePart.substring(linkStart, linkEnd)
            } catch (e: Exception) {}
        }

        return PaginationData(next)
    }

    data class PaginationData(val next: String?)
}
