package com.commit451.gitlab.tools;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.commit451.gitlab.adapter.IssuesAdapter;
import com.commit451.gitlab.adapter.NewUserAdapter;
import com.commit451.gitlab.model.Branch;
import com.commit451.gitlab.model.DiffLine;
import com.commit451.gitlab.model.Group;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.TreeItem;
import com.commit451.gitlab.model.User;

import java.util.ArrayList;

public class Repository {
	
	public static ArrayList<Project> projects;
	public static ArrayList<Branch> branches;
	public static ArrayList<Group> groups;
	public static ArrayList<User> users;
	
	public static Project selectedProject;
	public static Branch selectedBranch;
	public static Issue selectedIssue;
	public static TreeItem selectedFile;
	public static User selectedUser;
	public static DiffLine selectedCommit;
	
	public static DiffLine newestCommit;
	
	public static float displayWidth;
	
	public static void init(Context context) {
		
		projects = null;
		branches = null;
		groups = null;
		users = null;
		
		selectedProject = null;
		selectedBranch = null;
		selectedIssue = null;
		selectedFile = null;
		selectedUser = null;
		
		newestCommit = null;
	}

	public static void setListViewSize(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if(listAdapter == null)
			return;
		
		int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
		for(int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			if(listItem instanceof ViewGroup)
				listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += listItem.getMeasuredHeight();
		}
		
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}
}
