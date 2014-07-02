package com.bd.gitlab.model;

public class Branch {
	private String name;
	private boolean protected_;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isProtected() {
		return protected_;
	}
	public void setProtected(boolean protected_) {
		this.protected_ = protected_;
	}
	
	public String toString() {
		return name;
	}
}
