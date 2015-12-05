package com.commit451.gitlab.tools;

import android.net.Uri;
import android.text.TextUtils;

import com.commit451.gitlab.model.User;

import fr.tkeunebr.gravatar.Gravatar;

/**
 * Utility for doing various image related things
 * Created by Jawn on 9/20/2015.
 */
public class ImageUtil {
    private static Gravatar sGravatar;

    public static Uri getAvatarUrl(User user, int size) {
        if (user == null) {
            return getAvatarUrl("", size);
        }

//        String avatarUrl = user.getAvatarUrl();
//        if (avatarUrl != null && !avatarUrl.equals(Uri.EMPTY)) {
//            return avatarUrl.buildUpon()
//                    .appendQueryParameter("s", Integer.toString(size))
//                    .build();
//        }

        return getAvatarUrl(user.getEmail(), size);
    }

    public static Uri getAvatarUrl(String email, int size) {
        if (sGravatar == null) {
            sGravatar = new Gravatar.Builder().ssl().build();
        }

        if (!TextUtils.isEmpty(email)) {
            return Uri.parse(sGravatar.with(email).size(size).defaultImage(Gravatar.DefaultImage.IDENTICON).build());
        }

        return Uri.parse("https://secure.gravatar.com/avatar/00000000000000000000000000000000?d=identicon&f=y&s=" + size);
    }
}
