package com.bd.gitlab.model;

import java.util.Date;

public class User {
	
	private long id;
	private String username;
    private String email;
	private String avatar_url;
	private String name;
	private boolean blocked;
	private Date created_at;
	private int access_level;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

	public String getAvatarUrl() {
		return avatar_url;
	}
	public void setAvatarUrl(String avatar_url) {
		this.avatar_url = avatar_url;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean isBlocked() {
		return blocked;
	}
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public Date getCreatedAt() {
		return created_at;
	}
	public void setCreatedAt(Date created_at) {
		this.created_at = created_at;
	}

	public String getAccessLevel(String[] names) {
		int temp = access_level / 10 - 1;
		
		if(temp >= 0 && temp < names.length)
			return names[temp];
		
		return "";
	}
	public void setAccessLevel(int access_level) {
		this.access_level = access_level;
	}
	
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof User))
			return false;

		User rhs = (User) obj;

        return rhs.id == id;
	}
}
