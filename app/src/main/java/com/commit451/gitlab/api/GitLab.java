package com.commit451.gitlab.api;

import com.commit451.gitlab.model.api.Branch;
import com.commit451.gitlab.model.api.Contributor;
import com.commit451.gitlab.model.api.Diff;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.GroupDetail;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.model.api.RepositoryFile;
import com.commit451.gitlab.model.api.RepositoryTreeObject;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.model.api.UserLogin;

import java.util.List;

import retrofit.Call;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.Url;

public interface GitLab {
    String API_VERSION = "api/v3";

    /* --- LOGIN --- */

    @FormUrlEncoded
    @POST(API_VERSION + "/session")
    Call<UserLogin> loginWithUsername(@Field("login") String login,
                                      @Field("password") String password);

    @FormUrlEncoded
    @POST(API_VERSION + "/session")
    Call<UserLogin> loginWithEmail(@Field("email") String email,
                                   @Field("password") String password);

    /* --- USERS --- */

    /**
     * Get currently authenticated user
     */
    @GET(API_VERSION + "/user")
    Call<UserFull> getThisUser();

    @GET(API_VERSION + "/users?per_page=100")
    Call<List<UserBasic>> getUsers();

    @GET
    Call<List<UserBasic>> getUsers(@Url String url);

    @GET(API_VERSION + "/users?per_page=100")
    Call<List<UserBasic>> searchUsers(@Query("search") String query);

    @GET(API_VERSION + "/users/{id}")
    Call<UserBasic> getUser(@Path("id") long userId);

    /* --- GROUPS --- */

    @GET(API_VERSION + "/groups?per_page=100")
    Call<List<Group>> getGroups();

    @GET
    Call<List<Group>> getGroups(@Url String url);

    @GET(API_VERSION + "/groups/{id}")
    Call<GroupDetail> getGroup(@Path("id") long id);

    @GET(API_VERSION + "/groups/{id}/members?per_page=100")
    Call<List<Member>> getGroupMembers(@Path("id") long groupId);

    @FormUrlEncoded
    @POST(API_VERSION + "/groups/{id}/members")
    Call<Member> addGroupMember(@Path("id") long groupId,
                                @Field("user_id") long userId,
                                @Field("access_level") int accessLevel);

    @FormUrlEncoded
    @PUT(API_VERSION + "/groups/{id}/members/{user_id}")
    Call<Member> editGroupMember(@Path("id") long groupId,
                                 @Path("user_id") long userId,
                                 @Field("access_level") int accessLevel);

    @DELETE(API_VERSION + "/groups/{id}/members/{user_id}")
    Call<Void> removeGroupMember(@Path("id") long groupId,
                                 @Path("user_id") long userId);

    /* --- PROJECTS --- */

    @GET(API_VERSION + "/projects?order_by=last_activity_at")
    Call<List<Project>> getAllProjects();

    @GET(API_VERSION + "/projects/owned?order_by=last_activity_at")
    Call<List<Project>> getMyProjects();

    @GET(API_VERSION + "/projects/starred")
    Call<List<Project>> getStarredProjects();

    @GET(API_VERSION + "/projects/{id}")
    Call<Project> getProject(@Path("id") long projectId);

    @GET
    Call<List<Project>> getProjects(@Url String url);

    @GET(API_VERSION + "/projects/search/{query}")
    Call<List<Project>> searchAllProjects(@Path("query") String query);

    @GET(API_VERSION + "/projects/{id}/members?per_page=100")
    Call<List<Member>> getProjectMembers(@Path("id") long projectId);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/members")
    Call<Member> addProjectMember(@Path("id") long projectId,
                                  @Field("user_id") long userId,
                                  @Field("access_level") int accessLevel);

    @FormUrlEncoded
    @PUT(API_VERSION + "/projects/{id}/members/{user_id}")
    Call<Member> editProjectMember(@Path("id") long projectId,
                                   @Path("user_id") long userId,
                                   @Field("access_level") int accessLevel);

    @DELETE(API_VERSION + "/projects/{id}/members/{user_id}")
    Call<Void> removeProjectMember(@Path("id") long projectId,
                                   @Path("user_id") long userId);

    /* --- MILESTONES --- */

    @GET(API_VERSION + "/projects/{id}/milestones?per_page=100")
    Call<List<Milestone>> getMilestones(@Path("id") long projectId);

    @GET
    Call<List<Milestone>> getMilestones(@Url String url);

    @GET(API_VERSION + "/projects/{id}/milestones/{milestone_id}/issues?per_page=100")
    Call<List<Issue>> getMilestoneIssues(@Path("id") long projectId,
                                        @Path("milestone_id") long milestoneId);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/milestones")
    Call<Milestone> createMilestone(@Path("id") long projectId,
                            @Field("title") String title,
                            @Field("description") String description,
                            @Field("due_date") String dueDate);

    @FormUrlEncoded
    @PUT(API_VERSION + "/projects/{id}/milestones/{milestone_id}")
    Call<Milestone> editMilestone(@Path("id") long projectId,
                            @Path("milestone_id") long milestoneId,
                            @Field("title") String title,
                            @Field("description") String description,
                            @Field("due_date") String dueDate);

    /* --- MERGE REQUESTS --- */

    @GET(API_VERSION + "/projects/{id}/merge_requests?per_page=100")
    Call<List<MergeRequest>> getMergeRequests(@Path("id") long projectId);

    @GET(API_VERSION + "/projects/{id}/merge_requests?per_page=100")
    Call<List<MergeRequest>> getMergeRequests(@Path("id") long projectId,
                                              @Query("state") String state);

    @GET
    Call<List<MergeRequest>> getMergeRequests(@Url String url);

    @GET(API_VERSION + "/projects/{id}/merge_request/{merge_request_id}")
    Call<MergeRequest> getMergeRequest(@Path("id") long projectId,
                                       @Path("merge_request_id") long mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes?per_page=100")
    Call<List<Note>> getMergeRequestNotes(@Path("id") long projectId,
                                          @Path("merge_request_id") long mergeRequestId);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes")
    Call<Note> addMergeRequestNote(@Path("id") long projectId,
                                   @Path("merge_request_id") long mergeRequestId,
                                   @Field("note") String body);

    /* --- ISSUES --- */

    @GET(API_VERSION + "/projects/{id}/issues")
    Call<List<Issue>> getIssues(@Path("id") long projectId,
                                @Query("state") String state,
                                @Query("page") int page);

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Call<Issue> getIssue(@Path("id") long projectId,
                         @Path("issue_id") long issueId);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/issues")
    Call<Issue> createIssue(@Path("id") long projectId,
                            @Field("title") String title,
                            @Field("description") String description);

    @PUT(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Call<Issue> updateIssue(@Path("id") long projectId,
                            @Path("issue_id") long issueId,
                            @Query("title") String title,
                            @Query("description") String description);

    @PUT(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Call<Issue> updateIssueStatus(@Path("id") long projectId,
                                  @Path("issue_id") long issueId,
                                  @Query("state_event") @Issue.EditState String status);

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}/notes?order_by=last_activity_at&per_page=100")
    Call<List<Note>> getIssueNotes(@Path("id") long projectId,
                                   @Path("issue_id") long issueId);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/issues/{issue_id}/notes")
    Call<Note> addIssueNote(@Path("id") long projectId,
                            @Path("issue_id") long issueId,
                            @Field("body") String body);

    /* --- REPOSITORY --- */

    @GET(API_VERSION + "/projects/{id}/repository/branches?per_page=100&order_by=last_activity_at")
    Call<List<Branch>> getBranches(@Path("id") long projectId);

    @GET(API_VERSION + "/projects/{id}/repository/contributors?per_page=100")
    Call<List<Contributor>> getContributors(@Path("id") long projectId);

    @GET(API_VERSION + "/projects/{id}/repository/tree?per_page=100")
    Call<List<RepositoryTreeObject>> getTree(@Path("id") long projectId,
                                             @Query("ref_name") String branchName,
                                             @Query("path") String path);

    @GET(API_VERSION + "/projects/{id}/repository/files")
    Call<RepositoryFile> getFile(@Path("id") long projectId,
                                 @Query("file_path") String path,
                                 @Query("ref") String ref);

    @GET(API_VERSION + "/projects/{id}/repository/commits")
    Call<List<RepositoryCommit>> getCommits(@Path("id") long projectId,
                                            @Query("ref_name") String branchName,
                                            @Query("page") int page);

    @GET(API_VERSION + "/projects/{id}/repository/commits/{sha}")
    Call<RepositoryCommit> getCommit(@Path("id") long projectId,
                                     @Path("sha") String commitSHA);

    @GET(API_VERSION + "/projects/{id}/repository/commits/{sha}/diff")
    Call<List<Diff>> getCommitDiff(@Path("id") long projectId,
                                   @Path("sha") String commitSHA);
}