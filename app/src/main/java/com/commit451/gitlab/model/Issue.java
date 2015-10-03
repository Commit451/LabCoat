package com.commit451.gitlab.model;

import org.parceler.Parcel;

import java.util.Date;
@Parcel
public class Issue {
	
	long id;
	long iid;
	long project_id;
	String title;
	String description;
	String[] labels;
	Milestone milestone;
	User assignee;
	User author;
	String state;
	Date updated_at;
	Date created_at;

	public Issue(){}
	
	public long getId() {
		return id;
	}
	
	public long getIid() {
		return iid;
	}
	
	public long getProjectId() {
		return project_id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String[] getLabels() {
		return labels;
	}
	
	public Milestone getMilestone() {
		return milestone;
	}
	
	public User getAssignee() {
		return assignee;
	}
	
	public User getAuthor() {
		return author;
	}
	
	public String getState() {
		return state;
	}
	
	public Date getUpdatedAt() {
		return updated_at;
	}
	
	public Date getCreatedAt() {
		return created_at;
	}

	public String getUrl(Project project) {
		return project.getWebUrl() + "/issues/" + getId();
	}
}
