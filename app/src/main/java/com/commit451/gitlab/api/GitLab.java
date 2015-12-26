package com.commit451.gitlab.api;

import com.commit451.gitlab.model.api.Branch;
import com.commit451.gitlab.model.api.Contributor;
import com.commit451.gitlab.model.api.Diff;
import com.commit451.gitlab.model.api.Group;
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
    Call<UserLogin> getSessionByUsername(@Field("login") String login,
                                         @Field("password") String password);

    @FormUrlEncoded
    @POST(API_VERSION + "/session")
    Call<UserLogin> getSessionByEmail(@Field("email") String email,
                                      @Field("password") String password);

    /* --- MAIN --- */

    @GET(API_VERSION + "/groups?per_page=100")
    Call<List<Group>> getGroups();

    @GET(API_VERSION + "/users?per_page=100")
    Call<List<UserBasic>> getUsers();

    @GET(API_VERSION + "/projects?order_by=last_activity_at")
    Call<List<Project>> getAllProjects();

    @GET(API_VERSION + "/projects/owned?order_by=last_activity_at")
    Call<List<Project>> getMyProjects();

    @GET(API_VERSION + "/projects?order_by=last_activity_at")
    Call<List<Project>> searchAllProjects(@Query("search") String query);

    @GET
    Call<List<Project>> getProjectsNextPage(@Url String url);

    /* --- PROJECTS --- */

    @GET(API_VERSION + "/projects/{id}/repository/branches?per_page=100&order_by=last_activity_at")
    Call<List<Branch>> getBranches(@Path("id") long projectId);

    @GET(API_VERSION + "/projects/{id}/milestones?per_page=100")
    Call<List<Milestone>> getMilestones(@Path("id") long projectId);

    @GET(API_VERSION + "/projects/{id}/repository/contributors")
    Call<List<Contributor>> getContributors(@Path("id") long projectId);

    @GET(API_VERSION + "/projects/{id}/merge_requests?state=opened")
    Call<List<MergeRequest>> getMergeRequests(@Path("id") long projectId,
                                              @Query("state") String state);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes")
    Call<List<Note>> getMergeRequestNotes(@Path("id") long projectId,
                                          @Path("merge_request_id") long mergeRequestId);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes")
    Call<Note> postMergeRequestComment(@Path("id") long projectId,
                                       @Path("merge_request_id") long mergeRequestId,
                                       @Field("note") String body);

    @GET(API_VERSION + "/projects/{id}/members")
    Call<List<Member>> getProjectTeamMembers(@Path("id") long projectId);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/members")
    Call<Member> addProjectTeamMember(@Path("id") long projectId,
                                      @Field("user_id") long userId,
                                      @Field("access_level") int accessLevel);

    @FormUrlEncoded
    @PUT(API_VERSION + "/projects/{id}/members/{user_id}")
    Call<Member> editProjectTeamMember(@Path("id") long projectId,
                                       @Path("user_id") long userId,
                                       @Field("access_level") int accessLevel);

    @DELETE(API_VERSION + "/projects/{id}/members/{user_id}")
    Call<Void> removeProjectTeamMember(@Path("id") long projectId,
                                       @Path("user_id") long userId);

    /* --- COMMITS --- */

    @GET(API_VERSION + "/projects/{id}/repository/commits?per_page=100")
    Call<List<RepositoryCommit>> getCommits(@Path("id") long projectId,
                                            @Query("ref_name") String branchName);

    @GET(API_VERSION + "/projects/{id}/repository/commits/{sha}")
    Call<RepositoryCommit> getCommit(@Path("id") long projectId,
                                     @Path("sha") String commitSHA);

    @GET(API_VERSION + "/projects/{id}/repository/commits/{sha}/diff")
    Call<List<Diff>> getCommitDiff(@Path("id") long projectId,
                                   @Path("sha") String commitSHA);

    /* --- ISSUE --- */

    @GET(API_VERSION + "/projects/{id}/issues?per_page=100")
    Call<List<Issue>> getIssues(@Path("id") long projectId,
                                @Query("state") String state);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/issues")
    Call<Issue> postIssue(@Path("id") long projectId,
                          @Field("title") String title,
                          @Field("description") String description);

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}/notes?order_by=last_activity_at")
    Call<List<Note>> getIssueNotes(@Path("id") long projectId,
                                   @Path("issue_id") long issueId);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/issues/{issue_id}/notes")
    Call<Note> postIssueNote(@Path("id") long projectId,
                             @Path("issue_id") long issueId,
                             @Field("body") String body);

    @PUT(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Call<Issue> setIssueStatus(@Path("id") long projectId,
                               @Path("issue_id") long issueId,
                               @Query("state_event") @Issue.EditState String status);

    @PUT(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Call<Issue> updateIssue(@Path("id") long projectId,
                            @Path("issue_id") long issueId,
                            @Query("title") String title,
                            @Query("description") String description);

    /* --- FILES --- */

    @GET(API_VERSION + "/projects/{id}/repository/tree?per_page=100")
    Call<List<RepositoryTreeObject>> getTree(@Path("id") long projectId,
                                             @Query("ref_name") String branchName,
                                             @Query("path") String path);

    @GET(API_VERSION + "/projects/{id}/repository/files")
    Call<RepositoryFile> getFile(@Path("id") long projectId,
                                 @Query("file_path") String path,
                                 @Query("ref") String ref);
    /* --- USER --- */

    @GET(API_VERSION + "/users/{id}")
    Call<UserBasic> getUser(@Path("id") long userId);

    /**
     * Get currently authenticated user
     */
    @GET(API_VERSION + "/user")
    Call<UserFull> getUser();

    // Groups
    // https://github.com/gitlabhq/gitlabhq/blob/master/doc/api/groups.md

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

    @GET(API_VERSION + "/groups/{id}")
    Call<Group> getGroupDetails(@Path("id") long id);

    @GET(API_VERSION + "/users")
    Call<List<UserBasic>> searchUsers(@Query("search") String query);
}