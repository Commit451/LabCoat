package com.bd.gitlab.views;

public interface DrawableClickListener {
    public static enum DrawablePosition { TOP, RIGHT, BOTTOM, LEFT };
    public void onClick(DrawablePosition target);
}
