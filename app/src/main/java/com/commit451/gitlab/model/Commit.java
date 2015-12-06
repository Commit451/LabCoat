package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Parcel
public class Commit {
    @SerializedName("id")
    String mId;
    @SerializedName("short_id")
    String mShortId;
    @SerializedName("title")
    String mTitle;
    @SerializedName("author_name")
    String mAuthorName;
    @SerializedName("author_email")
    String mAuthorEmail;
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("message")
    String mMessage;

    public Commit() {}

    public String getId() {
        return mId;
    }

    public String getShortId() {
        return mShortId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthorName() {
        return mAuthorName;
    }

    public String getAuthorEmail() {
        return mAuthorEmail;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public String getMessage() {
        return mMessage;
    }

    public List<Line> getLines() {
        if (mMessage == null) {
            return null;
        }

        List<Line> lines = new ArrayList<>();

        String[] temp = mMessage.split("\\r?\\n");
        for (String s : temp) {
            Line line = new Line();
            line.lineContent = s;

            lines.add(line);
        }

        return lines;
    }

    public class Line {
        public String lineContent;
    }
}
