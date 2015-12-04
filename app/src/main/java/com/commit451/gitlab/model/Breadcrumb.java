package com.commit451.gitlab.model;

public class Breadcrumb {
    private final String mTitle;
    private final Listener mListener;

    public Breadcrumb(String title, Listener listener) {
        mTitle = title;
        mListener = listener;
    }

    public String getTitle() {
        return mTitle;
    }

    public Listener getListener() {
        return mListener;
    }

    public interface Listener {
        void onClick();
    }
}
