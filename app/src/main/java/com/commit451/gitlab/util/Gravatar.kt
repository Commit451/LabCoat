package com.commit451.gitlab.util

import android.net.Uri

import com.commit451.gitlab.ssl.X509Util

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Gravatar {

    @JvmOverloads
    fun init(email: String? = null): Builder {

        return Builder(email)
    }

    private fun md5(raw: String): String {
        try {
            val digest = MessageDigest.getInstance("MD5")
            digest.update(raw.toByteArray(Charset.forName("UTF-8")))
            return X509Util.hexify(digest.digest())
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException(e)
        }

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
                uriBuilder.append(md5(mEmail))
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
            if (query.length > 0) {
                uriBuilder.append("?").append(query.substring(1))
            }

            return Uri.parse(uriBuilder.toString())
        }
    }

    enum class Rating {
        G, PG, R, X
    }

    enum class DefaultImage {
        _404, MYSTERY_MAN, IDENTICON, MONSTERID, WAVATAR, RETRO, BLANK
    }
}
