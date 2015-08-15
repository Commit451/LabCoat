package com.commit451.gitlab.model;

import org.parceler.Parcel;

@Parcel
public class Group {
	
	long id;
	String name;
	String path;
	long owner_id;

	public Group(){}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public long getOwnerId() {
		return owner_id;
	}
	public void setOwnerId(long owner_id) {
		this.owner_id = owner_id;
	}
}
