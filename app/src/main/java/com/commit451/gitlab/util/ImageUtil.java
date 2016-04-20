package com.commit451.gitlab.util;

import android.net.Uri;

import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.model.api.UserFull;

/**
 * Utility for doing various image related things
 */
public class ImageUtil {
    public static Uri getAvatarUrl(UserBasic user, int size) {
        if (user != null) {
            Uri avatarUrl = user.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.equals(Uri.EMPTY)) {
                return avatarUrl.buildUpon()
                        .appendQueryParameter("s", Integer.toString(size))
                        .build();
            }

            if (user instanceof UserFull) {
                return getAvatarUrl(((UserFull) user).getEmail(), size);
            }
        }

        return getAvatarUrl("", size);
    }

    public static Uri getAvatarUrl(String email, int size) {
        return Gravatar
                .init(email)
                .ssl()
                .size(size)
                .defaultImage(Gravatar.DefaultImage.IDENTICON)
                .build();
    }
}
