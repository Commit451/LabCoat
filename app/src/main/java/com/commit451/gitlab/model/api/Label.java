package com.commit451.gitlab.model.api;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.bluelinelabs.logansquare.annotation.JsonField;

import org.parceler.Parcel;

/**
 * A label
 */
@Parcel
public class Label {

    @JsonField(name = "color")
    String mColor;
    @JsonField(name = "name")
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
