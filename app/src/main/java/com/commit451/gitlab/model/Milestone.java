package com.commit451.gitlab.model;

import java.util.Date;

public class Milestone {
	 
	private long id;
	private String title;
	private String description;
	private Date due_date;
	private boolean closed;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Date getDueDate() {
		return due_date;
	}
	public void setDueDate(Date due_date) {
		this.due_date = due_date;
	}
	
	public boolean isClosed() {
		return closed;
	}
	public void setClosed(boolean closed) {
		this.closed = closed;
	}
	
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Milestone))
			return false;

		Milestone rhs = (Milestone) obj;

        return rhs.id == id;
	}
}
