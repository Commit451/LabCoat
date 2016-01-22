package com.commit451.gitlab.util;

import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Gravatar {
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private Gravatar() {}

    public static Builder init() {
        return init(null);
    }

    public static Builder init(String email) {
        if (email != null && email.isEmpty()) {
            email = null;
        }

        return new Builder(email);
    }

    private static String hexify(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars, 0, hexChars.length);
    }

    private static String md5(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(raw.getBytes(Charset.forName("UTF-8")));
            return hexify(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Builder {
        private final String mEmail;

        private boolean mSSL = false;
        private boolean mExtension = false;
        private int mSize = -1;
        private String mDefaultImage = null;
        private boolean mForceDefault = false;
        private String mRating = null;

        private Builder(String email) {
            this.mEmail = email;
        }

        public Builder ssl() {
            mSSL = true;
            return this;
        }

        public Builder extension() {
            mExtension = true;
            return this;
        }

        public Builder size(int size) {
            if (size < 1 || size > 2048) {
                throw new IllegalArgumentException("Image size must be from 1px up to 2048px");
            }

            mSize = size;
            return this;
        }

        public Builder defaultImage(DefaultImage defaultImage) {
            switch (defaultImage) {
                case _404:
                    mDefaultImage = "404";
                    break;
                case MYSTERY_MAN:
                    mDefaultImage = "mm";
                    break;
                case IDENTICON:
                    mDefaultImage = "identicon";
                    break;
                case MONSTERID:
                    mDefaultImage = "monsterid";
                    break;
                case WAVATAR:
                    mDefaultImage = "wavatar";
                    break;
                case RETRO:
                    mDefaultImage = "retro";
                    break;
                case BLANK:
                    mDefaultImage = "blank";
                    break;
            }

            return this;
        }

        public Builder defaultImage(Uri defaultImage) {
            try {
                mDefaultImage = URLEncoder.encode(defaultImage.toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }

            return this;
        }

        public Builder forceDefault() {
            mForceDefault = true;
            return this;
        }

        public Builder rating(Rating rating) {
            switch (rating) {
                case G:
                    mRating = "g";
                    break;
                case PG:
                    mRating = "pg";
                    break;
                case R:
                    mRating = "r";
                    break;
                case X:
                    mRating = "x";
                    break;
            }

            return this;
        }

        public Uri build() {
            StringBuilder uriBuilder = new StringBuilder();
            if (mSSL) {
                uriBuilder.append("https://secure.gravatar.com/avatar/");
            } else {
                uriBuilder.append("http://www.gravatar.com/avatar/");
            }
            if (mEmail != null) {
                uriBuilder.append(md5(mEmail));
            } else {
                uriBuilder.append("00000000000000000000000000000000");
            }
            if (mExtension) {
                uriBuilder.append(".jpg");
            }

            StringBuilder queryBuilder = new StringBuilder();
            if (mSize != -1) {
                queryBuilder.append("&s=").append(mSize);
            }
            if (mDefaultImage != null) {
                queryBuilder.append("&d=").append(mDefaultImage);
            }
            if (mForceDefault) {
                queryBuilder.append("&f=y");
            }
            if (mRating != null) {
                queryBuilder.append("&r=").append(mRating);
            }
            String query = queryBuilder.toString();
            if (query.length() > 0) {
                uriBuilder.append("?").append(query.substring(1));
            }

            return Uri.parse(uriBuilder.toString());
        }
    }

    public enum Rating {
        G, PG, R, X
    }

    public enum DefaultImage {
        _404, MYSTERY_MAN, IDENTICON, MONSTERID, WAVATAR, RETRO, BLANK
    }
}
