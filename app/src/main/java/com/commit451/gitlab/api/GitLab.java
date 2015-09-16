package com.commit451.gitlab.api;

import com.commit451.gitlab.model.Branch;
import com.commit451.gitlab.model.DeleteResponse;
import com.commit451.gitlab.model.Diff;
import com.commit451.gitlab.model.DiffLine;
import com.commit451.gitlab.model.FileResponse;
import com.commit451.gitlab.model.Group;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.model.Milestone;
import com.commit451.gitlab.model.Note;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.Session;
import com.commit451.gitlab.model.TreeItem;
import com.commit451.gitlab.model.User;

import java.util.List;

import retrofit.Call;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface GitLab {
	String API_VERSION = "/api/v3";
	/* --- LOGIN --- */
	
	@POST(API_VERSION + "/session")
	Call<Session> getSessionByUsername(@Query("login") String login,
                              @Query("password") String password);
	
	@POST(API_VERSION + "/session")
	Call<Session> getSessionByEmail(@Query("email") String email,
                           @Query("password") String password);
	
	/* --- MAIN --- */
	
	@GET(API_VERSION + "/groups?per_page=100")
	Call<List<Group>> getGroups();
	
	@GET(API_VERSION + "/users?per_page=100")
	Call<List<User>> getUsers();
	
	@GET(API_VERSION + "/projects?per_page=100")
	Call<List<Project>> getProjects();
	
	/* --- MISC --- */
	
	@GET(API_VERSION + "/projects/{id}/repository/branches?per_page=100")
	Call<List<Branch>> getBranches(@Path("id") long projectId);
	
	@GET(API_VERSION + "/projects/{id}/milestones?per_page=100")
	Call<List<Milestone>> getMilestones(@Path("id") long projectId);
	
	@GET(API_VERSION + "/projects/{id}/members?per_page=100")
	Call<List<User>> getUsersFallback(@Path("id") long projectId);
	
	/* --- COMMITS --- */
	
	@GET(API_VERSION + "/projects/{id}/repository/commits?per_page=100")
	Call<List<DiffLine>> getCommits(@Path("id") long projectId,
                    @Query("ref_name") String branchName);

	@GET(API_VERSION + "/projects/{id}/repository/commits/{sha}")
	Call<DiffLine> getCommit(@Path("id") long projectId,
                   @Path("sha") String commitSHA);
	
	@GET(API_VERSION + "/projects/{id}/repository/commits/{sha}/diff")
	Call<List<Diff>> getCommitDiff(@Path("id") long projectId,
                       @Path("sha") String commitSHA);

	/* --- ISSUE --- */
	
	@GET(API_VERSION + "/projects/{id}/issues?per_page=100")
	Call<List<Issue>> getIssues(@Path("id") long projectId);
	
	@POST(API_VERSION + "/projects/{id}/issues")
	Call<Issue> postIssue(@Path("id") long projectId,
                   @Query("title") String title,
                   @Query("description") String description);
	
	@PUT(API_VERSION + "/projects/{id}/issues/{issue_id}")
	Call<Issue> editIssue(@Path("id") long projectId,
                   @Path("issue_id") long issueId,
                   @Query("state_event") String stateEvent,
                   @Query("assignee_id") long assigneeId,
                   @Query("milestone_id") long milestoneId);

	@PUT(API_VERSION + "/projects/{id}/issues/{issue_id}")
	Call<Issue> editIssue(@Path("id") long projectId,
				   @Path("issue_id") long issueId,
				   @Query("state_event") String stateEvent);
	
	@GET(API_VERSION + "/projects/{id}/issues/{issue_id}/notes?per_page=100")
	Call<List<Note>> getIssueNotes(@Path("id") long projectId,
                       @Path("issue_id") long issueId);
	
	@POST(API_VERSION + "/projects/{id}/issues/{issue_id}/notes")
	Call<Note> postIssueNote(@Path("id") long projectId,
                       @Path("issue_id") long issueId,
                       @Query("body") String body);
	
	/* --- FILES --- */
	
	@GET(API_VERSION + "/projects/{id}/repository/tree?per_page=100")
	Call<List<TreeItem>> getTree(@Path("id") long projectId,
                 @Query("ref_name") String branchName,
                 @Query("path") String path);

	@GET(API_VERSION + "/projects/{id}/repository/files")
	Call<FileResponse> getFile(@Path("id") long projectId,
				 @Query("file_path") String path,
				 @Query("ref") String ref);
	/* --- USER --- */

	@GET(API_VERSION + "/users/{id}")
	Call<User> getUser(@Path("id") long userId);
	
	@GET(API_VERSION + "/groups/{id}/members?per_page=100")
	Call<List<User>> getGroupMembers(@Path("id") long groupId);
	
	@POST(API_VERSION + "/groups/{id}/members")
	Call<User> addGroupMember(@Path("id") long groupId,
                        @Query("user_id") long userId,
                        @Query("access_level") String accessLevel);
	
	@DELETE(API_VERSION + "/groups/{id}/members/{user_id}")
	Call<DeleteResponse> removeGroupMember(@Path("id") long groupId,
                           @Path("user_id") long userId);
}