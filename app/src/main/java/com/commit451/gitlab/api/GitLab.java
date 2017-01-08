package com.commit451.gitlab.api;

import android.support.annotation.Nullable;

import com.commit451.gitlab.model.api.AwardEmoji;
import com.commit451.gitlab.model.api.Branch;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Contributor;
import com.commit451.gitlab.model.api.Diff;
import com.commit451.gitlab.model.api.FileUploadResponse;
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
import com.commit451.gitlab.model.api.Snippet;
import com.commit451.gitlab.model.api.Tag;
import com.commit451.gitlab.model.api.Todo;
import com.commit451.gitlab.model.api.User;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.model.api.UserLogin;

import java.util.List;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
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
    Single<Response<UserLogin>> loginWithUsername(@Field("login") String login,
                                                  @Field("password") String password);

    @FormUrlEncoded
    @POST(API_VERSION + "/session")
    Single<Response<UserLogin>> loginWithEmail(@Field("email") String email,
                                               @Field("password") String password);

    /* --- USERS --- */

    /**
     * Get currently authenticated user
     */
    @GET(API_VERSION + "/user")
    Single<Response<UserFull>> getThisUser();

    @GET(API_VERSION + "/users")
    Single<List<UserBasic>> getUsers();

    @GET
    Single<List<UserBasic>> getUsers(@Url String url);

    @GET(API_VERSION + "/users")
    Single<Response<List<UserBasic>>> searchUsers(@Query("search") String query);

    @GET
    Single<Response<List<UserBasic>>> searchUsers(@Url String url, @Query("search") String query);

    @GET(API_VERSION + "/users/{id}")
    Single<User> getUser(@Path("id") long userId);

    /* --- GROUPS --- */

    @GET(API_VERSION + "/groups")
    Single<Response<List<Group>>> getGroups();

    @GET
    Single<Response<List<Group>>> getGroups(@Url String url);

    @GET(API_VERSION + "/groups/{id}")
    Single<GroupDetail> getGroup(@Path("id") long id);

    @GET(API_VERSION + "/groups/{id}/projects?order_by=last_activity_at")
    Single<Response<List<Project>>> getGroupProjects(@Path("id") long id);

    @GET(API_VERSION + "/groups/{id}/members")
    Single<Response<List<Member>>> getGroupMembers(@Path("id") long groupId);

    @FormUrlEncoded
    @POST(API_VERSION + "/groups/{id}/members")
    Single<Response<Member>> addGroupMember(@Path("id") long groupId,
                                            @Field("user_id") long userId,
                                            @Field("access_level") int accessLevel);

    @FormUrlEncoded
    @PUT(API_VERSION + "/groups/{id}/members/{user_id}")
    Single<Member> editGroupMember(@Path("id") long groupId,
                                   @Path("user_id") long userId,
                                   @Field("access_level") int accessLevel);

    @DELETE(API_VERSION + "/groups/{id}/members/{user_id}")
    Single<String> removeGroupMember(@Path("id") long groupId,
                                     @Path("user_id") long userId);

    /* --- PROJECTS --- */

    @GET(API_VERSION + "/projects?order_by=last_activity_at&archived=false")
    Single<Response<List<Project>>> getAllProjects();

    @GET(API_VERSION + "/projects/owned?order_by=last_activity_at&archived=false")
    Single<Response<List<Project>>> getMyProjects();

    @GET(API_VERSION + "/projects/starred")
    Single<Response<List<Project>>> getStarredProjects();

    @GET(API_VERSION + "/projects/{id}")
    Single<Project> getProject(@Path("id") String projectId);

    // see https://github.com/gitlabhq/gitlabhq/blob/master/doc/api/projects.md#get-single-project
    @GET(API_VERSION + "/projects/{namespace}%2F{project_name}")
    Single<Project> getProject(@Path("namespace") String namespace,
                               @Path("project_name") String projectName);

    @GET
    Single<Response<List<Project>>> getProjects(@Url String url);

    @GET(API_VERSION + "/projects/search/{query}")
    Single<Response<List<Project>>> searchAllProjects(@Path("query") String query);

    @GET(API_VERSION + "/projects/{id}/members")
    Single<Response<List<Member>>> getProjectMembers(@Path("id") long projectId);

    @GET
    Single<Response<List<Member>>> getProjectMembers(@Url String url);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/members")
    Single<Response<Member>> addProjectMember(@Path("id") long projectId,
                                              @Field("user_id") long userId,
                                              @Field("access_level") int accessLevel);

    @FormUrlEncoded
    @PUT(API_VERSION + "/projects/{id}/members/{user_id}")
    Single<Member> editProjectMember(@Path("id") long projectId,
                                     @Path("user_id") long userId,
                                     @Field("access_level") int accessLevel);

    @DELETE(API_VERSION + "/projects/{id}/members/{user_id}")
    Single<String> removeProjectMember(@Path("id") long projectId,
                                     @Path("user_id") long userId);

    @POST(API_VERSION + "/projects/fork/{id}")
    Single<String> forkProject(@Path("id") long projectId);

    @POST(API_VERSION + "/projects/{id}/star")
    Single<Response<Project>> starProject(@Path("id") long projectId);

    @DELETE(API_VERSION + "/projects/{id}/star")
    Single<Project> unstarProject(@Path("id") long projectId);

    @Multipart
    @POST(API_VERSION + "/projects/{id}/uploads")
    Single<FileUploadResponse> uploadFile(@Path("id") long projectId,
                                          @Part MultipartBody.Part file);

    /* --- MILESTONES --- */

    @GET(API_VERSION + "/projects/{id}/milestones")
    Single<Response<List<Milestone>>> getMilestones(@Path("id") long projectId,
                                                    @Query("state") String state);

    @GET
    Single<Response<List<Milestone>>> getMilestones(@Url String url);

    @GET(API_VERSION + "/projects/{id}/issues")
    Single<List<Milestone>> getMilestonesByIid(@Path("id") long projectId,
                                               @Query("iid") String internalMilestoneId);

    @GET(API_VERSION + "/projects/{id}/milestones/{milestone_id}/issues")
    Single<Response<List<Issue>>> getMilestoneIssues(@Path("id") long projectId,
                                                     @Path("milestone_id") long milestoneId);

    @GET
    Single<Response<List<Issue>>> getMilestoneIssues(@Url String url);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/milestones")
    Single<Milestone> createMilestone(@Path("id") long projectId,
                                      @Field("title") String title,
                                      @Field("description") String description,
                                      @Field("due_date") String dueDate);

    @FormUrlEncoded
    @PUT(API_VERSION + "/projects/{id}/milestones/{milestone_id}")
    Single<Milestone> editMilestone(@Path("id") long projectId,
                                    @Path("milestone_id") long milestoneId,
                                    @Field("title") String title,
                                    @Field("description") String description,
                                    @Field("due_date") String dueDate);

    @PUT(API_VERSION + "/projects/{id}/milestones/{milestone_id}")
    Single<Milestone> updateMilestoneStatus(@Path("id") long projectId,
                                            @Path("milestone_id") long milestoneId,
                                            @Query("state_event") @Milestone.StateEvent String status);

    /* --- MERGE REQUESTS --- */

    @GET(API_VERSION + "/projects/{id}/merge_requests")
    Single<Response<List<MergeRequest>>> getMergeRequests(@Path("id") long projectId,
                                                          @Query("state") String state);

    @GET
    Single<Response<List<MergeRequest>>> getMergeRequests(@Url String url,
                                                          @Query("state") String state);

    @GET(API_VERSION + "/projects/{id}/merge_requests")
    Single<List<MergeRequest>> getMergeRequestsByIid(@Path("id") long projectId,
                                                     @Query("iid") String internalMergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_request/{merge_request_id}")
    Single<MergeRequest> getMergeRequest(@Path("id") long projectId,
                                         @Path("merge_request_id") long mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/commits")
    Single<List<RepositoryCommit>> getMergeRequestCommits(@Path("id") long projectId,
                                                          @Path("merge_request_id") long mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/changes")
    Single<MergeRequest> getMergeRequestChanges(@Path("id") long projectId,
                                                @Path("merge_request_id") long mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes")
    Single<Response<List<Note>>> getMergeRequestNotes(@Path("id") long projectId,
                                                      @Path("merge_request_id") long mergeRequestId);

    @GET
    Single<Response<List<Note>>> getMergeRequestNotes(@Url String url);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes")
    Single<Note> addMergeRequestNote(@Path("id") long projectId,
                                     @Path("merge_request_id") long mergeRequestId,
                                     @Field("body") String body);

    @PUT(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/merge")
    Single<Response<MergeRequest>> acceptMergeRequest(@Path("id") long projectId,
                                            @Path("merge_request_id") long mergeRequestId);

    /* --- ISSUES --- */

    @GET(API_VERSION + "/projects/{id}/issues")
    Single<Response<List<Issue>>> getIssues(@Path("id") long projectId,
                                            @Query("state") String state);

    @GET
    Single<Response<List<Issue>>> getIssues(@Url String url);

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Single<Issue> getIssue(@Path("id") long projectId,
                           @Path("issue_id") String issueId);

    @GET(API_VERSION + "/projects/{id}/issues")
    Single<List<Issue>> getIssuesByIid(@Path("id") long projectId,
                                       @Query("iid") String internalIssueId);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/issues")
    Single<Issue> createIssue(@Path("id") long projectId,
                              @Field("title") String title,
                              @Field("description") String description,
                              @Field("assignee_id") @Nullable Long assigneeId,
                              @Field("milestone_id") @Nullable Long milestoneId,
                              @Field("labels") @Nullable String commaSeparatedLabelNames);

    @PUT(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Single<Issue> updateIssue(@Path("id") long projectId,
                              @Path("issue_id") long issueId,
                              @Query("title") String title,
                              @Query("description") String description,
                              @Query("assignee_id") @Nullable Long assigneeId,
                              @Query("milestone_id") @Nullable Long milestoneId,
                              @Query("labels") @Nullable String commaSeparatedLabelNames);

    @PUT(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Single<Issue> updateIssueStatus(@Path("id") long projectId,
                                    @Path("issue_id") long issueId,
                                    @Query("state_event") @Issue.EditState String status);

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}/notes")
    Single<Response<List<Note>>> getIssueNotes(@Path("id") long projectId,
                                               @Path("issue_id") long issueId);

    @GET
    Single<Response<List<Note>>> getIssueNotes(@Url String url);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/issues/{issue_id}/notes")
    Single<Note> addIssueNote(@Path("id") long projectId,
                              @Path("issue_id") long issueId,
                              @Field("body") String body);

    @DELETE(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Single<String> deleteIssue(@Path("id") long projectId,
                               @Path("issue_id") long issueId);

    /* --- REPOSITORY --- */

    @GET(API_VERSION + "/projects/{id}/repository/branches?order_by=last_activity_at")
    Single<List<Branch>> getBranches(@Path("id") long projectId);

    @GET(API_VERSION + "/projects/{id}/repository/contributors")
    Single<List<Contributor>> getContributors(@Path("id") String projectId);

    @GET(API_VERSION + "/projects/{id}/repository/tree")
    Single<List<RepositoryTreeObject>> getTree(@Path("id") long projectId,
                                               @Query("ref_name") String branchName,
                                               @Query("path") String path);

    @GET(API_VERSION + "/projects/{id}/repository/files")
    Single<RepositoryFile> getFile(@Path("id") long projectId,
                                   @Query("file_path") String path,
                                   @Query("ref") String ref);

    @GET(API_VERSION + "/projects/{id}/repository/commits")
    Single<List<RepositoryCommit>> getCommits(@Path("id") long projectId,
                                              @Query("ref_name") String branchName,
                                              @Query("page") int page);

    @GET(API_VERSION + "/projects/{id}/repository/commits/{sha}")
    Single<RepositoryCommit> getCommit(@Path("id") long projectId,
                                       @Path("sha") String commitSHA);

    @GET(API_VERSION + "/projects/{id}/repository/commits/{sha}/diff")
    Single<List<Diff>> getCommitDiff(@Path("id") long projectId,
                                     @Path("sha") String commitSHA);

    /**
     * Get the current labels for a project
     *
     * @param projectId id
     * @return all the labels within a project
     */
    @GET(API_VERSION + "/projects/{id}/labels")
    Single<List<Label>> getLabels(@Path("id") long projectId);

    /**
     * Create a new label
     *
     * @param projectId id
     * @param name      the name of the label
     * @param color     the color, ex. #ff0000
     * @return Single
     */
    @POST(API_VERSION + "/projects/{id}/labels")
    Single<Response<Label>> createLabel(@Path("id") long projectId,
                              @Query("name") String name,
                              @Query("color") String color,
                              @Query("description") @Nullable String description);

    /**
     * Delete the label by its name
     *
     * @param projectId id
     * @return all the labels within a project
     */
    @DELETE(API_VERSION + "/projects/{id}/labels")
    Single<Label> deleteLabel(@Path("id") long projectId,
                              @Query("name") String name);


    /* --- BUILDS --- */
    @GET(API_VERSION + "/projects/{id}/builds")
    Single<Response<List<Build>>> getBuilds(@Path("id") long projectId,
                                            @Query("scope") String scope);

    @GET
    Single<Response<List<Build>>> getBuilds(@Url String url,
                                            @Query("scope") String state);

    @GET(API_VERSION + "/projects/{id}/builds/{build_id}")
    Single<Build> getBuild(@Path("id") long projectId,
                           @Path("build_id") long buildId);

    @POST(API_VERSION + "/projects/{id}/builds/{build_id}/retry")
    Single<Build> retryBuild(@Path("id") long projectId,
                             @Path("build_id") long buildId);

    @POST(API_VERSION + "/projects/{id}/builds/{build_id}/erase")
    Single<Build> eraseBuild(@Path("id") long projectId,
                             @Path("build_id") long buildId);

    @POST(API_VERSION + "/projects/{id}/builds/{build_id}/cancel")
    Single<Build> cancelBuild(@Path("id") long projectId,
                              @Path("build_id") long buildId);

    /* --- SNIPPETS --- */
    @GET(API_VERSION + "/projects/{id}/snippets")
    Single<Response<List<Snippet>>> getSnippets(@Path("id") long projectId);

    @GET
    Single<Response<List<Snippet>>> getSnippets(@Url String url);

    /* --- TODOS --- */
    @GET(API_VERSION + "/todos")
    Single<Response<List<Todo>>> getTodos(@Query("state") @Todo.State String state);

    @GET
    Single<Response<List<Todo>>> getTodosByUrl(@Url String url);

    /* --- TAGS --- */
    @GET(API_VERSION + "/projects/{id}/repository/tags")
    Single<List<Tag>> getTags(@Path("id") long projectId);

    /* --- AWARD EMOJI --- */
    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}/award_emoji")
    Single<List<AwardEmoji>> getAwardEmojiForIssue(@Path("id") long projectId,
                                                   @Path("issue_id") String issueId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/award_emoji")
    Single<List<AwardEmoji>> getAwardEmojiForMergeRequest(@Path("id") long projectId,
                                                          @Path("merge_request_id") String mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}/notes/{note_id}/award_emoji")
    Single<List<AwardEmoji>> getAwardEmojiForIssueNote(@Path("id") long projectId,
                                                       @Path("issue_id") String issueId,
                                                       @Path("note_id") String noteId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes/{note_id}/award_emoji")
    Single<List<AwardEmoji>> getAwardEmojiForMergeRequestNote(@Path("id") long projectId,
                                                              @Path("merge_request_id") String mergeRequestId,
                                                              @Path("note_id") String noteId);

    @POST(API_VERSION + "/projects/{id}/issues/{issue_id}/award_emoji")
    Single<AwardEmoji> postAwardEmojiForIssue(@Path("id") long projectId,
                                              @Path("issue_id") String issueId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/award_emoji")
    Single<AwardEmoji> postAwardEmojiForMergeRequest(@Path("id") long projectId,
                                                     @Path("merge_request_id") String mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes/{note_id}/award_emoji")
    Single<AwardEmoji> postAwardEmojiForMergeRequestNote(@Path("id") long projectId,
                                                         @Path("merge_request_id") String mergeRequestId,
                                                         @Path("note_id") String noteId);

    @POST(API_VERSION + "/projects/{id}/issues/{issue_id}/notes/{note_id}/award_emoji")
    Single<AwardEmoji> postAwardEmojiForIssueNote(@Path("id") long projectId,
                                                  @Path("issue_id") String issueId,
                                                  @Path("note_id") String noteId);

    @DELETE(API_VERSION + "/projects/{id}/issues/{issue_id}/award_emoji/{award_id}")
    Single<AwardEmoji> deleteAwardEmojiForIssue(@Path("id") long projectId,
                                                @Path("issue_id") String issueId,
                                                @Path("award_id") String awardId);

    @DELETE(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/award_emoji/{award_id}")
    Single<AwardEmoji> deleteAwardEmojiForMergeRequest(@Path("id") long projectId,
                                                       @Path("merge_request_id") String mergeRequestId,
                                                       @Path("award_id") String awardId);

    @DELETE(API_VERSION + "/projects/{id}/issues/{issue_id}/notes/{note_id}/award_emoji/{award_id}")
    Single<AwardEmoji> deleteAwardEmojiForIssueNote(@Path("id") long projectId,
                                                    @Path("issue_id") String issueId,
                                                    @Path("note_id") String noteId,
                                                    @Path("award_id") String awardId);

    @DELETE(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes/{note_id}/award_emoji/{award_id}")
    Single<AwardEmoji> deleteAwardEmojiForMergeRequestNote(@Path("id") long projectId,
                                                           @Path("merge_request_id") String mergeRequestId,
                                                           @Path("note_id") String noteId,
                                                           @Path("award_id") String awardId);

    /* --- MISC --- */
    @GET
    Single<String> getRaw(@Url String url);
}