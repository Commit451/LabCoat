package com.commit451.gitlab.model;

import com.commit451.gitlab.R;

import org.parceler.Parcel;

@Parcel
public class TreeItem {

	public static final String TYPE_FOLDER = "tree";
	public static final String TYPE_REPO = "submodule";
	public static final String TYPE_FILE = "blob";

	String name;
	String type;
	long mode;
	String id;

	public TreeItem(){}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public long getMode() {
		return mode;
	}
	
	public String getId() {
		return id;
	}

    public int getDrawableForType() {
        switch (type) {
            case TYPE_FILE:
                return R.drawable.ic_file_24dp;
            case TYPE_FOLDER:
                return R.drawable.ic_folder_24dp;
            case TYPE_REPO:
                return R.drawable.ic_repo_24dp;
        }
        return R.drawable.ic_file_24dp;
    }

    public String getUrl(Project project, String branchName, String currentPath) {
        String path = "/" + currentPath;
        return project.getWebUrl() + "/tree/" + branchName + path + name;
    }
}
