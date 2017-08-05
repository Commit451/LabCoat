package com.commit451.gitlab.util

import android.net.Uri
import com.commit451.gitlab.model.api.User

/**
 * Utility for doing various image related things
 */
object ImageUtil {
    fun getAvatarUrl(user: User?, size: Int): Uri {
        if (user != null) {

            if (user.avatarUrl != null) {
                val avatarUrl = Uri.parse(user.avatarUrl)
                if (avatarUrl != null && avatarUrl != Uri.EMPTY) {
                    return avatarUrl.buildUpon()
                            .appendQueryParameter("s", Integer.toString(size))
                            .build()
                }
            }

            val email = user.email
            if (email != null) {
                return getAvatarUrl(email, size)
            }
        }

        return getAvatarUrl(null as? String?, size)
    }

    fun getAvatarUrl(email: String?, size: Int): Uri {
        return Gravatar
                .init(email)
                .ssl()
                .size(size)
                .defaultImage(Gravatar.DefaultImage.IDENTICON)
                .build()
    }
}
