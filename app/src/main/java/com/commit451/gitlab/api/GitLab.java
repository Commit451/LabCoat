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
import rx.Observable;


/**
 * Defines the interface for Retrofit for the GitLab API
 * http://doc.gitlab.com/ce/api/README.html
 */
public interface GitLab {
    String API_VERSION = "api/v3";

    /* --- LOGIN --- */

    @FormUrlEncoded
    @POST(API_VERSION + "/session")
    Observable<Response<UserLogin>> loginWithUsername(@Field("login") String login,
                                                     @Field("password") String password);

    @FormUrlEncoded
    @POST(API_VERSION + "/session")
    Observable<Response<UserLogin>> loginWithEmail(@Field("email") String email,
                                   @Field("password") String password);

    /* --- USERS --- */

    /**
     * Get currently authenticated user
     */
    @GET(API_VERSION + "/user")
    Observable<Response<UserFull>> getThisUser();

    @GET(API_VERSION + "/users")
    Observable<List<UserBasic>> getUsers();

    @GET
    Observable<List<UserBasic>> getUsers(@Url String url);

    @GET(API_VERSION + "/users")
    Observable<Response<List<UserBasic>>> searchUsers(@Query("search") String query);

    @GET
    Observable<Response<List<UserBasic>>> searchUsers(@Url String url, @Query("search") String query);

    @GET(API_VERSION + "/users/{id}")
    Observable<User> getUser(@Path("id") long userId);

    /* --- GROUPS --- */

    @GET(API_VERSION + "/groups")
    Observable<Response<List<Group>>> getGroups();

    @GET
    Observable<Response<List<Group>>> getGroups(@Url String url);

    @GET(API_VERSION + "/groups/{id}")
    Observable<GroupDetail> getGroup(@Path("id") long id);

    @GET(API_VERSION + "/groups/{id}/projects?order_by=last_activity_at")
    Observable<Response<List<Project>>> getGroupProjects(@Path("id") long id);

    @GET(API_VERSION + "/groups/{id}/members")
    Observable<Response<List<Member>>> getGroupMembers(@Path("id") long groupId);

    @FormUrlEncoded
    @POST(API_VERSION + "/groups/{id}/members")
    Observable<Response<Member>> addGroupMember(@Path("id") long groupId,
                                @Field("user_id") long userId,
                                @Field("access_level") int accessLevel);

    @FormUrlEncoded
    @PUT(API_VERSION + "/groups/{id}/members/{user_id}")
    Observable<Member> editGroupMember(@Path("id") long groupId,
                                 @Path("user_id") long userId,
                                 @Field("access_level") int accessLevel);

    @DELETE(API_VERSION + "/groups/{id}/members/{user_id}")
    Observable<String> removeGroupMember(@Path("id") long groupId,
                                 @Path("user_id") long userId);

    /* --- PROJECTS --- */

    @GET(API_VERSION + "/projects?order_by=last_activity_at&archived=false")
    Observable<Response<List<Project>>> getAllProjects();

    @GET(API_VERSION + "/projects/owned?order_by=last_activity_at&archived=false")
    Observable<Response<List<Project>>> getMyProjects();

    @GET(API_VERSION + "/projects/starred")
    Observable<Response<List<Project>>> getStarredProjects();

    @GET(API_VERSION + "/projects/{id}")
    Observable<Project> getProject(@Path("id") String projectId);

    // see https://github.com/gitlabhq/gitlabhq/blob/master/doc/api/projects.md#get-single-project
    @GET(API_VERSION + "/projects/{namespace}%2F{project_name}")
    Observable<Project> getProject(@Path("namespace") String namespace,
                             @Path("project_name") String projectName);

    @GET
    Observable<Response<List<Project>>> getProjects(@Url String url);

    @GET(API_VERSION + "/projects/search/{query}")
    Observable<Response<List<Project>>> searchAllProjects(@Path("query") String query);

    @GET(API_VERSION + "/projects/{id}/members")
    Observable<Response<List<Member>>> getProjectMembers(@Path("id") long projectId);

    @GET
    Observable<Response<List<Member>>> getProjectMembers(@Url String url);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/members")
    Observable<Response<Member>> addProjectMember(@Path("id") long projectId,
                                  @Field("user_id") long userId,
                                  @Field("access_level") int accessLevel);

    @FormUrlEncoded
    @PUT(API_VERSION + "/projects/{id}/members/{user_id}")
    Observable<Member> editProjectMember(@Path("id") long projectId,
                                   @Path("user_id") long userId,
                                   @Field("access_level") int accessLevel);

    @DELETE(API_VERSION + "/projects/{id}/members/{user_id}")
    Observable<Void> removeProjectMember(@Path("id") long projectId,
                                   @Path("user_id") long userId);

    @POST(API_VERSION + "/projects/fork/{id}")
    Observable<String> forkProject(@Path("id") long projectId);

    @POST(API_VERSION + "/projects/{id}/star")
    Observable<Project> starProject(@Path("id") long projectId);

    @DELETE(API_VERSION + "/projects/{id}/star")
    Observable<Project> unstarProject(@Path("id") long projectId);

    @Multipart
    @POST(API_VERSION + "/projects/{id}/uploads")
    Observable<FileUploadResponse> uploadFile(@Path("id") long projectId,
                                        @Part MultipartBody.Part file);

    /* --- MILESTONES --- */

    @GET(API_VERSION + "/projects/{id}/milestones")
    Observable<Response<List<Milestone>>> getMilestones(@Path("id") long projectId,
                                        @Query("state") String state);

    @GET
    Observable<Response<List<Milestone>>> getMilestones(@Url String url);

    @GET(API_VERSION + "/projects/{id}/issues")
    Observable<List<Milestone>> getMilestonesByIid(@Path("id") long projectId,
                                             @Query("iid") String internalMilestoneId);

    @GET(API_VERSION + "/projects/{id}/milestones/{milestone_id}/issues")
    Observable<Response<List<Issue>>> getMilestoneIssues(@Path("id") long projectId,
                                         @Path("milestone_id") long milestoneId);

    @GET
    Observable<Response<List<Issue>>> getMilestoneIssues(@Url String url);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/milestones")
    Observable<Milestone> createMilestone(@Path("id") long projectId,
                                    @Field("title") String title,
                                    @Field("description") String description,
                                    @Field("due_date") String dueDate);

    @FormUrlEncoded
    @PUT(API_VERSION + "/projects/{id}/milestones/{milestone_id}")
    Observable<Milestone> editMilestone(@Path("id") long projectId,
                                  @Path("milestone_id") long milestoneId,
                                  @Field("title") String title,
                                  @Field("description") String description,
                                  @Field("due_date") String dueDate);

    @PUT(API_VERSION + "/projects/{id}/milestones/{milestone_id}")
    Observable<Milestone> updateMilestoneStatus(@Path("id") long projectId,
                                          @Path("milestone_id") long milestoneId,
                                          @Query("state_event") @Milestone.StateEvent String status);

    /* --- MERGE REQUESTS --- */

    @GET(API_VERSION + "/projects/{id}/merge_requests")
    Observable<Response<List<MergeRequest>>> getMergeRequests(@Path("id") long projectId,
                                              @Query("state") String state);

    @GET
    Observable<Response<List<MergeRequest>>> getMergeRequests(@Url String url,
                                              @Query("state") String state);

    @GET(API_VERSION + "/projects/{id}/merge_requests")
    Observable<List<MergeRequest>> getMergeRequestsByIid(@Path("id") long projectId,
                                                   @Query("iid") String internalMergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_request/{merge_request_id}")
    Observable<MergeRequest> getMergeRequest(@Path("id") long projectId,
                                       @Path("merge_request_id") long mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/commits")
    Observable<List<RepositoryCommit>> getMergeRequestCommits(@Path("id") long projectId,
                                                        @Path("merge_request_id") long mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/changes")
    Observable<MergeRequest> getMergeRequestChanges(@Path("id") long projectId,
                                              @Path("merge_request_id") long mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes")
    Observable<Response<List<Note>>> getMergeRequestNotes(@Path("id") long projectId,
                                          @Path("merge_request_id") long mergeRequestId);

    @GET
    Observable<Response<List<Note>>> getMergeRequestNotes(@Url String url);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes")
    Observable<Note> addMergeRequestNote(@Path("id") long projectId,
                                   @Path("merge_request_id") long mergeRequestId,
                                   @Field("body") String body);

    @PUT(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/merge")
    Observable<MergeRequest> acceptMergeRequest(@Path("id") long projectId,
                                          @Path("merge_request_id") long mergeRequestId);

    /* --- ISSUES --- */

    @GET(API_VERSION + "/projects/{id}/issues")
    Observable<Response<List<Issue>>> getIssues(@Path("id") long projectId,
                                @Query("state") String state);

    @GET
    Observable<Response<List<Issue>>> getIssues(@Url String url);

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Observable<Issue> getIssue(@Path("id") long projectId,
                         @Path("issue_id") String issueId);

    @GET(API_VERSION + "/projects/{id}/issues")
    Observable<List<Issue>> getIssuesByIid(@Path("id") long projectId,
                                     @Query("iid") String internalIssueId);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/issues")
    Observable<Issue> createIssue(@Path("id") long projectId,
                            @Field("title") String title,
                            @Field("description") String description,
                            @Field("assignee_id") @Nullable Long assigneeId,
                            @Field("milestone_id") @Nullable Long milestoneId,
                            @Field("labels") @Nullable String commaSeparatedLabelNames);

    @PUT(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Observable<Issue> updateIssue(@Path("id") long projectId,
                            @Path("issue_id") long issueId,
                            @Query("title") String title,
                            @Query("description") String description,
                            @Query("assignee_id") @Nullable Long assigneeId,
                            @Query("milestone_id") @Nullable Long milestoneId,
                            @Query("labels") @Nullable String commaSeparatedLabelNames);

    @PUT(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Observable<Issue> updateIssueStatus(@Path("id") long projectId,
                                  @Path("issue_id") long issueId,
                                  @Query("state_event") @Issue.EditState String status);

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}/notes")
    Observable<Response<List<Note>>> getIssueNotes(@Path("id") long projectId,
                                   @Path("issue_id") long issueId);

    @GET
    Observable<Response<List<Note>>> getIssueNotes(@Url String url);

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/issues/{issue_id}/notes")
    Observable<Note> addIssueNote(@Path("id") long projectId,
                            @Path("issue_id") long issueId,
                            @Field("body") String body);

    @DELETE(API_VERSION + "/projects/{id}/issues/{issue_id}")
    Observable<String> deleteIssue(@Path("id") long projectId,
                           @Path("issue_id") long issueId);

    /* --- REPOSITORY --- */

    @GET(API_VERSION + "/projects/{id}/repository/branches?order_by=last_activity_at")
    Observable<List<Branch>> getBranches(@Path("id") long projectId);

    @GET(API_VERSION + "/projects/{id}/repository/contributors")
    Observable<List<Contributor>> getContributors(@Path("id") String projectId);

    @GET(API_VERSION + "/projects/{id}/repository/tree")
    Observable<List<RepositoryTreeObject>> getTree(@Path("id") long projectId,
                                             @Query("ref_name") String branchName,
                                             @Query("path") String path);

    @GET(API_VERSION + "/projects/{id}/repository/files")
    Observable<RepositoryFile> getFile(@Path("id") long projectId,
                                 @Query("file_path") String path,
                                 @Query("ref") String ref);

    @GET(API_VERSION + "/projects/{id}/repository/commits")
    Observable<List<RepositoryCommit>> getCommits(@Path("id") long projectId,
                                            @Query("ref_name") String branchName,
                                            @Query("page") int page);

    @GET(API_VERSION + "/projects/{id}/repository/commits/{sha}")
    Observable<RepositoryCommit> getCommit(@Path("id") long projectId,
                                     @Path("sha") String commitSHA);

    @GET(API_VERSION + "/projects/{id}/repository/commits/{sha}/diff")
    Observable<List<Diff>> getCommitDiff(@Path("id") long projectId,
                                   @Path("sha") String commitSHA);

    /**
     * Get the current labels for a project
     *
     * @param projectId id
     * @return all the labels within a project
     */
    @GET(API_VERSION + "/projects/{id}/labels")
    Observable<List<Label>> getLabels(@Path("id") long projectId);

    /**
     * Create a new label
     *
     * @param projectId id
     * @param name      the name of the label
     * @param color     the color, ex. #ff0000
     * @return observable
     */
    @POST(API_VERSION + "/projects/{id}/labels")
    Observable<Label> createLabel(@Path("id") long projectId,
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
    Observable<Label> deleteLabel(@Path("id") long projectId,
                            @Query("name") String name);


    /* --- BUILDS --- */
    @GET(API_VERSION + "/projects/{id}/builds")
    Observable<Response<List<Build>>> getBuilds(@Path("id") long projectId,
                                @Query("scope") String scope);

    @GET
    Observable<Response<List<Build>>> getBuilds(@Url String url,
                                @Query("scope") String state);

    @GET(API_VERSION + "/projects/{id}/builds/{build_id}")
    Observable<Build> getBuild(@Path("id") long projectId,
                         @Path("build_id") long buildId);

    @POST(API_VERSION + "/projects/{id}/builds/{build_id}/retry")
    Observable<Build> retryBuild(@Path("id") long projectId,
                           @Path("build_id") long buildId);

    @POST(API_VERSION + "/projects/{id}/builds/{build_id}/erase")
    Observable<Build> eraseBuild(@Path("id") long projectId,
                           @Path("build_id") long buildId);

    @POST(API_VERSION + "/projects/{id}/builds/{build_id}/cancel")
    Observable<Build> cancelBuild(@Path("id") long projectId,
                            @Path("build_id") long buildId);

    /* --- SNIPPETS --- */
    @GET(API_VERSION + "/projects/{id}/snippets")
    Observable<Response<List<Snippet>>> getSnippets(@Path("id") long projectId);

    @GET
    Observable<Response<List<Snippet>>> getSnippets(@Url String url);

    /* --- TODOS --- */
    @GET(API_VERSION + "/todos")
    Observable<Response<List<Todo>>> getTodos(@Query("state") @Todo.State String state);

    @GET
    Observable<Response<List<Todo>>> getTodosByUrl(@Url String url);

    /* --- TAGS --- */
    @GET(API_VERSION + "/projects/{id}/repository/tags")
    Observable<List<Tag>> getTags(@Path("id") long projectId);

    /* --- AWARD EMOJI --- */
    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}/award_emoji")
    Observable<List<AwardEmoji>> getAwardEmojiForIssue(@Path("id") long projectId,
                                                 @Path("issue_id") String issueId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/award_emoji")
    Observable<List<AwardEmoji>> getAwardEmojiForMergeRequest(@Path("id") long projectId,
                                                        @Path("merge_request_id") String mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}/notes/{note_id}/award_emoji")
    Observable<List<AwardEmoji>> getAwardEmojiForIssueNote(@Path("id") long projectId,
                                                     @Path("issue_id") String issueId,
                                                     @Path("note_id") String noteId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes/{note_id}/award_emoji")
    Observable<List<AwardEmoji>> getAwardEmojiForMergeRequestNote(@Path("id") long projectId,
                                                            @Path("merge_request_id") String mergeRequestId,
                                                            @Path("note_id") String noteId);

    @POST(API_VERSION + "/projects/{id}/issues/{issue_id}/award_emoji")
    Observable<AwardEmoji> postAwardEmojiForIssue(@Path("id") long projectId,
                                            @Path("issue_id") String issueId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/award_emoji")
    Observable<AwardEmoji> postAwardEmojiForMergeRequest(@Path("id") long projectId,
                                                   @Path("merge_request_id") String mergeRequestId);

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes/{note_id}/award_emoji")
    Observable<AwardEmoji> postAwardEmojiForMergeRequestNote(@Path("id") long projectId,
                                                       @Path("merge_request_id") String mergeRequestId,
                                                       @Path("note_id") String noteId);

    @POST(API_VERSION + "/projects/{id}/issues/{issue_id}/notes/{note_id}/award_emoji")
    Observable<AwardEmoji> postAwardEmojiForIssueNote(@Path("id") long projectId,
                                                @Path("issue_id") String issueId,
                                                @Path("note_id") String noteId);

    @DELETE(API_VERSION + "/projects/{id}/issues/{issue_id}/award_emoji/{award_id}")
    Observable<AwardEmoji> deleteAwardEmojiForIssue(@Path("id") long projectId,
                                              @Path("issue_id") String issueId,
                                              @Path("award_id") String awardId);

    @DELETE(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/award_emoji/{award_id}")
    Observable<AwardEmoji> deleteAwardEmojiForMergeRequest(@Path("id") long projectId,
                                                     @Path("merge_request_id") String mergeRequestId,
                                                     @Path("award_id") String awardId);

    @DELETE(API_VERSION + "/projects/{id}/issues/{issue_id}/notes/{note_id}/award_emoji/{award_id}")
    Observable<AwardEmoji> deleteAwardEmojiForIssueNote(@Path("id") long projectId,
                                                  @Path("issue_id") String issueId,
                                                  @Path("note_id") String noteId,
                                                  @Path("award_id") String awardId);

    @DELETE(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes/{note_id}/award_emoji/{award_id}")
    Observable<AwardEmoji> deleteAwardEmojiForMergeRequestNote(@Path("id") long projectId,
                                                         @Path("merge_request_id") String mergeRequestId,
                                                         @Path("note_id") String noteId,
                                                         @Path("award_id") String awardId);

    /* --- MISC --- */
    @GET
    Observable<String> getRaw(@Url String url);
}