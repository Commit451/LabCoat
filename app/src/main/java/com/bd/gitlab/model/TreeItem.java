package com.bd.gitlab.model;

public class TreeItem {
	
	private String name;
	private String type;
	private long mode;
	private String id;
	
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
