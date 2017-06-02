package com.commit451.gitlab.util

import android.net.Uri

object Gravatar {

    fun init(email: String? = null): Builder {
        return Builder(email)
    }

    class Builder constructor(private val mEmail: String?) {

        private var mSSL = false
        private var mExtension = false
        private var mSize = -1
        private var mDefaultImage: String? = null
        private var mForceDefault = false
        private var mRating: String? = null

        fun ssl(): Builder {
            mSSL = true
            return this
        }

        fun extension(): Builder {
            mExtension = true
            return this
        }

        fun size(size: Int): Builder {
            if (size < 1 || size > 2048) {
                throw IllegalArgumentException("Image size must be from 1px up to 2048px")
            }

            mSize = size
            return this
        }

        fun defaultImage(defaultImage: DefaultImage): Builder {
            when (defaultImage) {
                Gravatar.DefaultImage._404 -> mDefaultImage = "404"
                Gravatar.DefaultImage.MYSTERY_MAN -> mDefaultImage = "mm"
                Gravatar.DefaultImage.IDENTICON -> mDefaultImage = "identicon"
                Gravatar.DefaultImage.MONSTERID -> mDefaultImage = "monsterid"
                Gravatar.DefaultImage.WAVATAR -> mDefaultImage = "wavatar"
                Gravatar.DefaultImage.RETRO -> mDefaultImage = "retro"
                Gravatar.DefaultImage.BLANK -> mDefaultImage = "blank"
            }

            return this
        }

        fun build(): Uri {
            val uriBuilder = StringBuilder()
            if (mSSL) {
                uriBuilder.append("https://secure.gravatar.com/avatar/")
            } else {
                uriBuilder.append("http://www.gravatar.com/avatar/")
            }
            if (mEmail != null) {
                uriBuilder.append(Hash.md5(mEmail))
            } else {
                uriBuilder.append("00000000000000000000000000000000")
            }
            if (mExtension) {
                uriBuilder.append(".jpg")
            }

            val queryBuilder = StringBuilder()
            if (mSize != -1) {
                queryBuilder.append("&s=").append(mSize)
            }
            if (mDefaultImage != null) {
                queryBuilder.append("&d=").append(mDefaultImage)
            }
            if (mForceDefault) {
                queryBuilder.append("&f=y")
            }
            if (mRating != null) {
                queryBuilder.append("&r=").append(mRating)
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
