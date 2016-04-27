package com.commit451.gitlab.api;

import android.support.annotation.Nullable;

import com.commit451.gitlab.model.api.Artifact;
import com.commit451.gitlab.model.api.Branch;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Contributor;
import com.commit451.gitlab.model.api.Diff;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.GroupDetail;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Label;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.model.api.RepositoryFile;
import com.commit451.gitlab.model.api.RepositoryTreeObject;
import com.commit451.gitlab.model.api.User;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.model.api.UserLogin;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;


/**
 * Defines the interface for Retrofit for the GitLab API
 * http://doc.gitlab.com/ce/api/README.html
 */
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

    @GET(API_VERSION + "/users")
    Call<List<UserBasic>> getUsers();

    @GET
    Call<List<UserBasic>> getUsers(@Url String url);

    @GET(API_VERSION + "/users")
    Call<List<UserBasic>> searchUsers(@Query("search") String query);

    @GET
    Call<List<UserBasic>> searchUsers(@Url String url, @Query("search") String query);

    @GET(API_VERSION + "/users/{id}")
    Call<User> getUser(@Path("id") long userId);

    /* --- GROUPS --- */

    @GET(API_VERSION + "/groups")
    Call<List<Group>> getGroups();

    @GET
    Call<List<Group>> getGroups(@Url String url);

    @GET(API_VERSION + "/groups/{id}")
    Call<GroupDetail> getGroup(@Path("id") long id);

    @GET(API_VERSION + "/groups/{id}/projects?order_by=last_activity_at")
    Call<List<Project>> getGroupProjects(@Path("id") long id);

    @GET(API_VERSION + "/groups/{id}/members")
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

    @GET(API_VERSION + "/projects?order_by=last_activity_at&archived=false")
    Call<List<Project>> getAllProjects();

    @GET(API_VERSION + "/projects/owned?order_by=last_activity_at&archived=false")
    Call<List<Project>> getMyProjects();

    @GET(API_VERSION + "/projects/starred")
    Call<List<Project>> getStarredProjects();

    @GET(API_VERSION + "/projects/{id}")
    Call<Project> getProject(@Path("id") String projectId);

    // see https://github.com/gitlabhq/gitlabhq/blob/master/doc/api/projects.md#get-single-project
    @GET(API_VERSION + "/projects/{namespace}%2F{project_name}")
    Call<Project> getProject(@Path("namespace") String namespace,
                             @Path("project_name") String projectName);

    @GET
    Call<List<Project>> getProjects(@Url String url);

    @GET(API_VERSION + "/projects/search/{query}")
    Call<List<Project>> searchAllProjects(@Path("query") String query);

    @GET(API_VERSION + "/projects/{id}/members")
    Call<List<Member>> getProjectMembers(@Path("id") long projectId);

    @GET
    Call<List<Member>> getProjectMembers(@Url String url);

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

    @POST(API_VERSION + "/projects/fork/{id}")
    Call<Void> forkProject(@Path("id") long projectId);

    @POST(API_VERSION + "/projects/{id}/star")
    Call<Project> starProject(@Path("id") long projectId);

    /* --- MILESTONES --- */

    @GET(API_VERSION + "/projects/{id}/milestones")
    Call<List<Milestone>> getMilestones(@Path("id") long projectId);

    @GET
    Call<List<Milestone>> getMilestones(@Url String url);

    @GET(API_VERSION + "/projects/{id}/milestones/{milestone_id}/issues")
    Call<List<Issue>> getMilestoneIssues(@Path("id") long projectId,
                                         @Path("milestone_id") long milestoneId);

    @GET
    Call<List<Issue>> getMilestoneIssues(@Url String url);

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

    @PUT(API_VERSION + "/projects/{id}/milestones/{milestone_id}")
    Call<Milestone> updateMilestoneStatus(@Path("id") long projectId,
                                          @Path("milestone_id") long milestoneId,
                                          @Query("state_event") @Milestone.StateEvent String status);

    /* --- MERGE REQUESTS --- */

    @GET(API_VERSION + "/projects/{id}/merge_requests")
    Call<List<MergeRequest>> getMergeRequests(@Path("id") long projectId,
                                              @Query("state") String state);

    @GET
    Call<List<MergeRequest>> getMergeRequests(@Url String url,
                                              @Query("state") String state);

    @GET(API_VERSION + "/projects/{id}/merge_request/{merge_request_id}")
    Call<MergeRequest> getMergeRequest(@Path("id") long projectId,
                                       @Path("merge_request_id") long mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes")
    Call<List<Note>> getMergeRequestNotes(@Path("id") long projectId,
                                          @Path("merge_request_id") long mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/commits")
    Call<List<RepositoryCommit>> getMergeRequestCommits(@Path("id") long projectId,
                                                        @Path("merge_request_id") long mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/changes")
    Call<MergeRequest> getMergeRequestChanges(@Path("id") long projectId,
                                              @Path("merge_request_id") long mergeRequestId);

    @GET
    Call<List<Note>> getMergeRequestNotes(@Url String url);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes")
    Call<Note> addMergeRequestNote(@Path("id") long projectId,
                                   @Path("merge_request_id") long mergeRequestId,
                                   @Field("body") String body);

    @PUT(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}")
    Call<MergeRequest> acceptMergeRequest(@Path("id") long projectId,
                                          @Path("merge_request_id") long mergeRequestId);

    /* --- ISSUES --- */

    @GET(API_VERSION + "/projects/{id}/issues")
    Call<List<Issue>> getIssues(@Path("id") long projectId,
                                @Query("state") String state);

    @GET
    Call<List<Issue>> getIssues(@Url String url,
                                @Query("state") String state);

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Call<Issue> getIssue(@Path("id") long projectId,
                         @Path("issue_id") String issueId);

    @GET(API_VERSION + "/projects/{id}/issues")
    Call<List<Issue>> getIssuesByIid(@Path("id") long projectId,
                         @Query("iid") String internalIssueId);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/issues")
    Call<Issue> createIssue(@Path("id") long projectId,
                            @Field("title") String title,
                            @Field("description") String description,
                            @Field("assignee_id") @Nullable Long assigneeId,
                            @Field("milestone_id") @Nullable Long milestoneId);

    @PUT(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Call<Issue> updateIssue(@Path("id") long projectId,
                            @Path("issue_id") long issueId,
                            @Query("title") String title,
                            @Query("description") String description,
                            @Query("assignee_id") @Nullable Long assigneeId,
                            @Query("milestone_id") @Nullable Long milestoneId);

    @PUT(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Call<Issue> updateIssueStatus(@Path("id") long projectId,
                                  @Path("issue_id") long issueId,
                                  @Query("state_event") @Issue.EditState String status);

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}/notes")
    Call<List<Note>> getIssueNotes(@Path("id") long projectId,
                                   @Path("issue_id") long issueId);

    @GET
    Call<List<Note>> getIssueNotes(@Url String url);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/issues/{issue_id}/notes")
    Call<Note> addIssueNote(@Path("id") long projectId,
                            @Path("issue_id") long issueId,
                            @Field("body") String body);

    /* --- REPOSITORY --- */

    @GET(API_VERSION + "/projects/{id}/repository/branches?order_by=last_activity_at")
    Call<List<Branch>> getBranches(@Path("id") long projectId);

    @GET(API_VERSION + "/projects/{id}/repository/contributors")
    Call<List<Contributor>> getContributors(@Path("id") String projectId);

    @GET(API_VERSION + "/projects/{id}/repository/tree")
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

    /**
     * Get the current labels for a project
     *
     * @param projectId id
     * @return all the labels within a project
     */
    @GET(API_VERSION + "/projects/{id}/labels")
    Call<List<Label>> getLabels(@Path("id") long projectId);

    /**
     * Create a new label
     *
     * @param projectId id
     * @param name      the name of the label
     * @param color     the color, ex. #ff0000
     * @return call onSuccess the newly created label
     */
    @POST(API_VERSION + "/projects/{id}/labels")
    Call<Label> createLabel(@Path("id") long projectId,
                            @Query("name") String name,
                            @Query("color") String color);

    /**
     * Delete the label by its name
     *
     * @param projectId id
     * @return all the labels within a project
     */
    @DELETE(API_VERSION + "/projects/{id}/labels")
    Call<Label> deleteLabel(@Path("id") long projectId,
                            @Query("name") String name);


    /* --- BUILDS --- */
    @GET(API_VERSION + "/projects/{id}/builds")
    Call<List<Build>> getBuilds(@Path("id") long projectId,
                                @Query("scope") String scope);

    @GET
    Call<List<Build>> getBuilds(@Url String url,
                                @Query("scope") String state);

    @GET(API_VERSION + "/projects/{id}/builds/{build_id}")
    Call<Build> getBuild(@Path("id") long projectId,
                         @Path("build_id") long buildId);

    @POST(API_VERSION + "/projects/{id}/builds/{build_id}/retry")
    Call<Build> retryBuild(@Path("id") long projectId,
                           @Path("build_id") long buildId);

    @POST(API_VERSION + "/projects/{id}/builds/{build_id}/erase")
    Call<Build> eraseBuild(@Path("id") long projectId,
                           @Path("build_id") long buildId);

    @POST(API_VERSION + "/projects/{id}/builds/{build_id}/cancel")
    Call<Build> cancelBuild(@Path("id") long projectId,
                            @Path("build_id") long buildId);

    @GET(API_VERSION + "/projects/{id}/builds/{build_id}/artifacts")
    Call<List<Artifact>> getBuildArtifacts(@Path("id") long projectId,
                                           @Path("build_id") long buildId);
}