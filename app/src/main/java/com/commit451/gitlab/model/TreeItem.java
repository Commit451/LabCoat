package com.commit451.gitlab.model;

import org.parceler.Parcel;

@Parcel
public class TreeItem {
	
	String name;
	String type;
	long mode;
	String id;

	public TreeItem(){}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public long getMode() {
		return mode;
	}
	public void setMode(long mode) {
		this.mode = mode;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
