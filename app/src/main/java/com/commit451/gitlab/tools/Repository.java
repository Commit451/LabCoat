package com.commit451.gitlab.tools;

import android.content.Context;

import com.commit451.gitlab.model.Branch;
import com.commit451.gitlab.model.Group;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.User;

import java.util.ArrayList;

public class Repository {
	
	public static ArrayList<Project> projects;
	public static ArrayList<Branch> branches;
	public static ArrayList<Group> groups;
	public static ArrayList<User> users;
	
	public static Project selectedProject;
	public static Branch selectedBranch;
	public static User selectedUser;
	
	public static float displayWidth;
	
	public static void init(Context context) {
		
		projects = null;
		branches = null;
		groups = null;
		users = null;
		
		selectedProject = null;
		selectedBranch = null;
		selectedUser = null;
	}
}
