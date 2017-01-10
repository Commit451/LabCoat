package com.commit451.gitlab.util

import android.net.Uri

import com.commit451.gitlab.model.api.UserBasic
import com.commit451.gitlab.model.api.UserFull

/**
 * Utility for doing various image related things
 */
object ImageUtil {
    fun getAvatarUrl(user: UserBasic?, size: Int): Uri {
        if (user != null) {
            val avatarUrl = user.avatarUrl
            if (avatarUrl != null && avatarUrl != Uri.EMPTY) {
                return avatarUrl.buildUpon()
                        .appendQueryParameter("s", Integer.toString(size))
                        .build()
            }

            if (user is UserFull) {
                return getAvatarUrl(user.email, size)
            }
        }

        return getAvatarUrl("", size)
    }

    fun getAvatarUrl(email: String, size: Int): Uri {
        return Gravatar
                .init(email)
                .ssl()
                .size(size)
                .defaultImage(Gravatar.DefaultImage.IDENTICON)
                .build()
    }
}
