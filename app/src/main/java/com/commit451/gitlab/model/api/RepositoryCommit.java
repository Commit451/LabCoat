package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import com.commit451.gitlab.util.ObjectUtil;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Parcel
public class RepositoryCommit {
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

    public RepositoryCommit() {}

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RepositoryCommit)) {
            return false;
        }

        RepositoryCommit commit = (RepositoryCommit) o;
        return ObjectUtil.equals(mId, commit.mId);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hash(mId);
    }

    public class Line {
        public String lineContent;
    }
}
