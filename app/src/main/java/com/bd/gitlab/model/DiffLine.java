package com.bd.gitlab.model;

import java.util.Date;

public class DiffLine {
	
	private String id;
	private String short_id;
	private String title;
	private String author_name;
	private String author_email;
	private Date created_at;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getShortId() {
		return short_id;
	}
	public void setShortId(String short_id) {
		this.short_id = short_id;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getAuthorName() {
		return author_name;
	}
	public void setAuthorName(String author_name) {
		this.author_name = author_name;
	}
	
	public String getAuthorEmail() {
		return author_email;
	}
	public void setAuthorEmail(String author_email) {
		this.author_email = author_email;
	}
	
	public Date getCreatedAt() {
		return created_at;
	}
	public void setCreatedAt(Date created_at) {
		this.created_at = created_at;
	}
}
