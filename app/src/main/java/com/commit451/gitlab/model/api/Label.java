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
    @JsonField(name = "description")
    String mDescription;
    @JsonField(name = "open_issues_count")
    int mOpenIssuesCount;
    @JsonField(name = "closed_issues_count")
    int mClosedIssuesCount;
    @JsonField(name = "open_merge_requests_count")
    int mOpenMergeRequestsCount;
    @JsonField(name = "subscribed")
    boolean mSubscribed;

    protected Label() {
        //for json parsing
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

    public String getDescription() {
        return mDescription;
    }

    public int getOpenIssuesCount() {
        return mOpenIssuesCount;
    }

    public int getClosedIssuesCount() {
        return mClosedIssuesCount;
    }

    public int getOpenMergeRequestsCount() {
        return mOpenMergeRequestsCount;
    }

    public boolean isSubscribed() {
        return mSubscribed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Label label = (Label) o;

        if (mColor != null ? !mColor.equals(label.mColor) : label.mColor != null) return false;
        if (mName != null ? !mName.equals(label.mName) : label.mName != null) return false;
        return mDescription != null ? mDescription.equals(label.mDescription) : label.mDescription == null;

    }

    @Override
    public int hashCode() {
        int result = mColor != null ? mColor.hashCode() : 0;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mDescription != null ? mDescription.hashCode() : 0);
        return result;
    }
}
