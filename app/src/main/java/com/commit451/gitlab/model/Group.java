package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Group {
	@SerializedName("id")
	long mId;
	@SerializedName("name")
    String mName;
    @SerializedName("description")
    String mDescription;
    @SerializedName("path")
	String mPath;
    @SerializedName("avatar_url")
    String mAvatarUrl;
    @SerializedName("web_url")
    String mWebUrl;

	public Group(){}
	
	public long getId() {
		return mId;
	}
	
	public String getName() {
		return mName;
	}

    public String getDescription() {
        return mDescription;
    }

    public String getPath() {
		return mPath;
	}

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public String getWebUrl() {
        return mWebUrl;
    }
}
