package com.commit451.gitlab.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.commit451.gitlab.adapter.IssuesAdapter;
import com.commit451.gitlab.adapter.UserAdapter;
import com.commit451.gitlab.model.Branch;
import com.commit451.gitlab.model.DiffLine;
import com.commit451.gitlab.model.Group;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.TreeItem;
import com.commit451.gitlab.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

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

	public static IssuesAdapter issueAdapter;
	public static UserAdapter userAdapter;
	
	public static float displayWidth;
	
	private static final String LOGGED_IN = "logged_in";
	private static final String SERVER_URL = "server_url";
	private static final String PRIVATE_TOKEN = "private_token";
    private static final String LAST_PROJECT = "last_project";
    private static final String LAST_BRANCH = "last_branch";
	
	private static SharedPreferences preferences;
	private static GitLabAPI service;
	
	public static void init(Context context) {
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
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

		issueAdapter = null;
		userAdapter = null;
	}
	
	public static GitLabAPI getService() {
		if(getServerUrl().length() < 1) {
			Repository.setLoggedIn(false);
			Repository.setPrivateToken("");
			Repository.setServerUrl("");
			
			service = null;
			
			return null;
		}
		
		if(service == null) {
			// Configure Gson to handle dates correctly
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
				@Override
				public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                    return ISODateTimeFormat.dateTimeParser().parseDateTime(json.getAsString()).toDate();
				}
			});
			Gson gson = gsonBuilder.create();
			
			RestAdapter restAdapter = new RestAdapter.Builder().setRequestInterceptor(new GitLabInterceptor()).setConverter(new GsonConverter(gson)).setEndpoint(getServerUrl() + "/api/v3").build();
			service = restAdapter.create(GitLabAPI.class);
		}
		
		return service;
	}
	
	public static boolean isLoggedIn() {
		return preferences.getBoolean(LOGGED_IN, false);
	}
	
	public static void setLoggedIn(boolean loggedIn) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(LOGGED_IN, loggedIn);
		editor.commit();
	}
	
	public static void setServerUrl(String serverUrl) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(SERVER_URL, serverUrl);
		editor.commit();

        service = null;
	}
	
	public static String getServerUrl() {
		return preferences.getString(SERVER_URL, "");
	}
	
	public static void setPrivateToken(String privateToken) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PRIVATE_TOKEN, privateToken);
		editor.commit();
	}
	
	public static String getPrivateToken() {
		return preferences.getString(PRIVATE_TOKEN, "");
	}

    public static void setLastProject(String lastProject) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_PROJECT, lastProject);
        editor.commit();
    }

    public static String getLastProject() {
        return preferences.getString(LAST_PROJECT, "");
    }

    public static void setLastBranch(String lastBranch) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_BRANCH, lastBranch);
        editor.commit();
    }

    public static String getLastBranch() {
        return preferences.getString(LAST_BRANCH, "");
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
	
	public static void resetService() {
		service = null;
	}
}
