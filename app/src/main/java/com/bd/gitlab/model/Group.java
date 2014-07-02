package com.bd.gitlab.model;

public class Group {
	
	private long id;
	private String name;
	private String path;
	private long owner_id;
	
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
