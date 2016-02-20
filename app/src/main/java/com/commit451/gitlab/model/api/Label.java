package com.commit451.gitlab.model.api;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * A label
 */
@Parcel
public class Label {

    @SerializedName("color")
    String mColor;
    @SerializedName("name")
    String mName;

    protected Label() {
        //for json parsing
    }

    public Label(String name, String color) {
        mName = name;
        mColor = color;
    }

    public String getName() {
        return mName;
    }

    public @ColorInt int getColor() {
        try {
            return Color.parseColor(mColor);
        } catch (IllegalArgumentException e) {
            return Color.TRANSPARENT;
        }
    }
}
