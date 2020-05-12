package com.commit451.gitlab.util

import android.net.Uri

object Gravatar {

    fun init(email: String? = null): Builder {
        return Builder(email)
    }

    class Builder constructor(private val mEmail: String?) {

        private var ssl = false
        private var extension = false
        private var size = -1
        private var defaultImage: String? = null
        private var forceDefault = false
        private var rating: String? = null

        fun ssl(): Builder {
            ssl = true
            return this
        }

        fun extension(): Builder {
            extension = true
            return this
        }

        fun size(size: Int): Builder {
            if (size < 1 || size > 2048) {
                throw IllegalArgumentException("Image size must be from 1px up to 2048px")
            }

            this.size = size
            return this
        }

        fun defaultImage(defaultImage: DefaultImage): Builder {
            when (defaultImage) {
                DefaultImage._404 -> this.defaultImage = "404"
                DefaultImage.MYSTERY_MAN -> this.defaultImage = "mm"
                DefaultImage.IDENTICON -> this.defaultImage = "identicon"
                DefaultImage.MONSTERID -> this.defaultImage = "monsterid"
                DefaultImage.WAVATAR -> this.defaultImage = "wavatar"
                DefaultImage.RETRO -> this.defaultImage = "retro"
                DefaultImage.BLANK -> this.defaultImage = "blank"
            }

            return this
        }

        fun build(): Uri {
            val uriBuilder = StringBuilder()
            if (ssl) {
                uriBuilder.append("https://secure.gravatar.com/avatar/")
            } else {
                uriBuilder.append("http://www.gravatar.com/avatar/")
            }
            if (mEmail != null) {
                uriBuilder.append(Hash.md5(mEmail))
            } else {
                uriBuilder.append("00000000000000000000000000000000")
            }
            if (extension) {
                uriBuilder.append(".jpg")
            }

            val queryBuilder = StringBuilder()
            if (size != -1) {
                queryBuilder.append("&s=").append(size)
            }
            if (defaultImage != null) {
                queryBuilder.append("&d=").append(defaultImage)
            }
            if (forceDefault) {
                queryBuilder.append("&f=y")
            }
            if (rating != null) {
                queryBuilder.append("&r=").append(rating)
            }
            val query = queryBuilder.toString()
            if (query.isNotEmpty()) {
                uriBuilder.append("?").append(query.substring(1))
            }

            return Uri.parse(uriBuilder.toString())
        }
    }

    enum class DefaultImage {
        _404, MYSTERY_MAN, IDENTICON, MONSTERID, WAVATAR, RETRO, BLANK
    }
}
