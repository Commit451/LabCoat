package com.commit451.gitlab.model;

import com.commit451.gitlab.tools.Repository;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class Project {
	
	long id;
	String name;
	String description;
	String default_branch;
	User owner;
	boolean is_public;
	String path;
	String path_with_namespace;
	boolean issues_enabled;
	boolean merge_requests_enabled;
	boolean wall_enabled;
	boolean wiki_enabled;
	Date created_at;
	@SerializedName("avatar_url")
	String avatarUrl;

	public Project(){}
	
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
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDefaultBranch() {
		return default_branch;
	}
	
	public void setDefaultBranch(String default_branch) {
		this.default_branch = default_branch;
	}
	
	public User getOwner() {
		return owner;
	}
	
	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	public boolean isPublic() {
		return is_public;
	}
	
	public void setPublic(boolean is_public) {
		this.is_public = is_public;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPathWithNamespace() {
		return path_with_namespace;
	}
	
	public void setPathWithNamespace(String path_with_namespace) {
		this.path_with_namespace = path_with_namespace;
	}
	
	public boolean isIssuesEnabled() {
		return issues_enabled;
	}
	
	public void setIssuesEnabled(boolean issues_enabled) {
		this.issues_enabled = issues_enabled;
	}
	
	public boolean isMergeRequestsEnabled() {
		return merge_requests_enabled;
	}
	
	public void setMergeRequestsEnabled(boolean merge_requests_enabled) {
		this.merge_requests_enabled = merge_requests_enabled;
	}
	
	public boolean isWallEnabled() {
		return wall_enabled;
	}
	
	public void setWallEnabled(boolean wall_enabled) {
		this.wall_enabled = wall_enabled;
	}
	
	public boolean isWikiEnabled() {
		return wiki_enabled;
	}
	
	public void setWikiEnabled(boolean wiki_enabled) {
		this.wiki_enabled = wiki_enabled;
	}
	
	public Date getCreatedAt() {
		return created_at;
	}
	
	public void setCreatedAt(Date created_at) {
		this.created_at = created_at;
	}
	
	public String toString() {
		Group g = getGroup();
		
		if(g != null)
			return g.getName() + " / " + name;
		
		if(path_with_namespace != null)
			return path_with_namespace.split("/")[0] + " / " + name;
		
		else
			return name;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public Group getGroup() {
		if(Repository.groups == null)
			return null;
		
		for(Group g : Repository.groups)
			if(g.getPath().equals(this.path_with_namespace.split("/")[0]))
				return g;
		
		return null;
	}
	
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj == this)
			return true;
		if(!(obj instanceof Project))
			return false;
		
		Project rhs = (Project) obj;

        return rhs.id == this.id;
	}
}
