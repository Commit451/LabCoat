package com.commit451.gitlab.util

import android.net.Uri

import retrofit2.Response
import timber.log.Timber

/**
 * Parses link headers from a retrofit [Response]
 * @see [https://www.w3.org/wiki/LinkHeader](https://www.w3.org/wiki/LinkHeader)
 */
object LinkHeaderParser {

    private val PREV_PAGE_SUFFIX = "rel=\"prev\""
    private val NEXT_PAGE_SUFFIX = "rel=\"next\""
    private val FIRST_PAGE_SUFFIX = "rel=\"first\""
    private val LAST_PAGE_SUFFIX = "rel=\"last\""

    fun parse(response: Response<*>): PaginationData {
        var prev: Uri? = null
        var next: Uri? = null
        var first: Uri? = null
        var last: Uri? = null

        val header = response.headers().get("Link")
        if (header != null) {
            val parts = header.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (part in parts) {
                try {
                    val linkStart = part.indexOf('<') + 1
                    val linkEnd = part.indexOf('>')

                    val link = Uri.parse(part.substring(linkStart, linkEnd))

                    if (part.contains(PREV_PAGE_SUFFIX)) {
                        prev = link
                    } else if (part.contains(NEXT_PAGE_SUFFIX)) {
                        next = link
                    } else if (part.contains(FIRST_PAGE_SUFFIX)) {
                        first = link
                    } else if (part.contains(LAST_PAGE_SUFFIX)) {
                        last = link
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }

            }
        }

        return PaginationData(prev, next, first, last)
    }

    class PaginationData internal constructor(val prev: Uri?, val next: Uri?, val first: Uri?, val last: Uri?)
}
