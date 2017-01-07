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
    String color;
    @JsonField(name = "name")
    String name;
    @JsonField(name = "description")
    String description;
    @JsonField(name = "open_issues_count")
    int openIssuesCount;
    @JsonField(name = "closed_issues_count")
    int closedIssuesCount;
    @JsonField(name = "open_merge_requests_count")
    int openMergeRequestsCount;
    @JsonField(name = "subscribed")
    boolean subscribed;

    protected Label() {
        //for json parsing
    }

    public String getName() {
        return name;
    }

    @ColorInt
    public int getColor() {
        try {
            return Color.parseColor(color);
        } catch (Exception e) {
            return Color.TRANSPARENT;
        }
    }

    public String getDescription() {
        return description;
    }

    public int getOpenIssuesCount() {
        return openIssuesCount;
    }

    public int getClosedIssuesCount() {
        return closedIssuesCount;
    }

    public int getOpenMergeRequestsCount() {
        return openMergeRequestsCount;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Label label = (Label) o;

        if (color != null ? !color.equals(label.color) : label.color != null) return false;
        if (name != null ? !name.equals(label.name) : label.name != null) return false;
        return description != null ? description.equals(label.description) : label.description == null;

    }

    @Override
    public int hashCode() {
        int result = color != null ? color.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
