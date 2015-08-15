package com.commit451.gitlab.model;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Parcel
public class DiffLine {
	
	String id;
	String short_id;
	String title;
	String author_name;
	String author_email;
	Date created_at;
	String message;

	public DiffLine(){}
	
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

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	public List<Line> getLines() {
		ArrayList<Line> lines = new ArrayList<Line>();

		String[] temp = message.split("\\r?\\n");

		for(String s : temp) {
			Line line = new Line();
			line.lineContent = s;

			lines.add(line);
		}

		return lines;
	}

	public class Line {
		public String lineContent;
	}
}
