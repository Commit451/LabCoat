package com.commit451.gitlab.tools;

import android.text.TextUtils;

import com.commit451.gitlab.model.User;

import fr.tkeunebr.gravatar.Gravatar;

/**
 * Utility for doing various image related things
 * Created by Jawn on 9/20/2015.
 */
public class ImageUtil {

    public static String getAvatarUrl(User user, int size) {
        if (user == null) {
            return getAvatarUrl("", size);
        }

        String avatarUrl = user.getAvatarUrl();
        if (!TextUtils.isEmpty(avatarUrl)) {
            if (avatarUrl.contains("?")) {
                return avatarUrl + "&s=" + size;
            } else {
                return avatarUrl + "?s=" + size;
            }
        }

        return getAvatarUrl(user.getEmail(), size);
    }

    public static String getAvatarUrl(String email, int size) {
        if (!TextUtils.isEmpty(email)) {
            return Gravatar.init().with(email).size(size).defaultImage(Gravatar.DefaultImage.IDENTICON).build();
        }

        return "http://www.gravatar.com/avatar/00000000000000000000000000000000?d=identicon&f=y&s=" + size;
    }
}
