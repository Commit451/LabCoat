package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;
@Parcel
public class Note {

	@SerializedName("id")
	long id;
	@SerializedName("body")
	String body;
    @SerializedName("author")
	User author;
    @SerializedName("created_at")
	Date created_at;

	public Note(){}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	public User getAuthor() {
		return author;
	}
	public void setAuthor(User author) {
		this.author = author;
	}
	
	public Date getCreatedAt() {
		return created_at;
	}
	public void setCreatedAt(Date created_at) {
		this.created_at = created_at;
	}
}
