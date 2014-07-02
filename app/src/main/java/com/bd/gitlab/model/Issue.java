package com.bd.gitlab.model;

import java.util.Date;

public class Issue {
	
	private long id;
	private long iid;
	private long project_id;
	private String title;
	private String description;
	private String[] labels;
	private Milestone milestone;
	private User assignee;
	private User author;
	private String state;
	private Date updated_at;
	private Date created_at;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public long getIid() {
		return iid;
	}
	public void setIid(long iid) {
		this.iid = iid;
	}
	
	public long getProjectId() {
		return project_id;
	}
	public void setProjectId(long project_id) {
		this.project_id = project_id;
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
	
	public String[] getLabels() {
		return labels;
	}
	public void setLabels(String[] labels) {
		this.labels = labels;
	}
	
	public Milestone getMilestone() {
		return milestone;
	}
	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}
	
	public User getAssignee() {
		return assignee;
	}
	public void setAssignee(User assignee) {
		this.assignee = assignee;
	}
	
	public User getAuthor() {
		return author;
	}
	public void setAuthor(User author) {
		this.author = author;
	}
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
	public Date getUpdatedAt() {
		return updated_at;
	}
	public void setUpdatedAt(Date updated_at) {
		this.updated_at = updated_at;
	}
	
	public Date getCreatedAt() {
		return created_at;
	}
	public void setCreatedAt(Date created_at) {
		this.created_at = created_at;
	}
}
