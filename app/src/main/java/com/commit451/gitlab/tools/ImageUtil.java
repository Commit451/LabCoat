package com.commit451.gitlab.tools;

import android.text.TextUtils;

import com.commit451.gitlab.model.User;

import fr.tkeunebr.gravatar.Gravatar;

/**
 * Created by Jawn on 9/20/2015.
 */
public class ImageUtil {

    public static String getGravatarUrl(User user, int size) {
        if (user.getAvatarUrl() != null) {
            return user.getAvatarUrl() + "&s=" + size;
        }
        return getGravatarUrl(user.getEmail(), size);

    }

    public static String getGravatarUrl(String email, int size) {
        if(!TextUtils.isEmpty(email)) {
            return Gravatar.init().with(email).size(size).build();
        }

        return "http://www.gravatar.com/avatar/00000000000000000000000000000000?s=" + size;
    }
}
