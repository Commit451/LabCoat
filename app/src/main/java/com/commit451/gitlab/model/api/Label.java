package com.commit451.gitlab.model.api;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

/**
 * A label
 */
@Parcel
@JsonObject
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

    @ColorInt
    public int getColor() {
        try {
            return Color.parseColor(mColor);
        } catch (Exception e) {
            return Color.TRANSPARENT;
        }
    }
}
