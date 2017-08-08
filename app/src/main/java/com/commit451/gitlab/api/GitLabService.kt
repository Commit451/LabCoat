package com.commit451.gitlab.api

import com.commit451.gitlab.api.request.SessionRequest
import com.commit451.gitlab.api.response.FileUploadResponse
import com.commit451.gitlab.model.api.*
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*


/**
 * Defines the interface for Retrofit for the GitLabService API
 * http://doc.gitlab.com/ce/api/README.html
 */
interface GitLabService {

    companion object {
        const val API_VERSION = "api/v4"
        const val TREE_PER_PAGE = "100"
    }

    /* --- LOGIN --- */

    @POST(API_VERSION + "/session")
    fun login(@Body request: SessionRequest): Single<Response<User>>

    /* --- USERS --- */

    /**
     * Get currently authenticated user
     */
    @GET(API_VERSION + "/user")
    fun getThisUser(): Single<Response<User>>

    @GET(API_VERSION + "/users")
    fun getUsers(): Single<List<User>>

    @GET
    fun getUsers(@Url url: String): Single<List<User>>

    @GET(API_VERSION + "/users")
    fun searchUsers(@Query("search") query: String): Single<Response<List<User>>>

    @GET
    fun searchUsers(@Url url: String, @Query("search") query: String): Single<Response<List<User>>>

    @GET(API_VERSION + "/users/{id}")
    fun getUser(@Path("id") userId: Long): Single<User>

    /* --- GROUPS --- */
    @GET(API_VERSION + "/groups")
    fun getGroups(): Single<Response<List<Group>>>

    @GET
    fun getGroups(@Url url: String): Single<Response<List<Group>>>

    @GET(API_VERSION + "/groups/{id}")
    fun getGroup(@Path("id") id: Long): Single<Group>

    @GET(API_VERSION + "/groups/{id}/projects?order_by=last_activity_at")
    fun getGroupProjects(@Path("id") id: Long): Single<Response<List<Project>>>

    @GET(API_VERSION + "/groups/{id}/members")
    fun getGroupMembers(@Path("id") groupId: Long): Single<Response<List<User>>>

    @FormUrlEncoded
    @POST(API_VERSION + "/groups/{id}/members")
    fun addGroupMember(@Path("id") groupId: Long,
                       @Field("user_id") userId: Long,
                       @Field("access_level") accessLevel: Int): Single<Response<User>>

    @FormUrlEncoded
    @PUT(API_VERSION + "/groups/{id}/members/{user_id}")
    fun editGroupMember(@Path("id") groupId: Long,
                        @Path("user_id") userId: Long,
                        @Field("access_level") accessLevel: Int): Single<User>

    @DELETE(API_VERSION + "/groups/{id}/members/{user_id}")
    fun removeGroupMember(@Path("id") groupId: Long,
                          @Path("user_id") userId: Long): Single<String>

    /* --- PROJECTS --- */

    @GET(API_VERSION + "/projects?membership=true&order_by=last_activity_at&archived=false")
    fun getAllProjects(): Single<Response<List<Project>>>

    @GET(API_VERSION + "/projects?owned=true&order_by=last_activity_at&archived=false")
    fun getMyProjects(): Single<Response<List<Project>>>

    @GET(API_VERSION + "/projects?starred=true")
    fun getStarredProjects(): Single<Response<List<Project>>>

    @GET(API_VERSION + "/projects/{id}")
    fun getProject(@Path("id") projectId: String): Single<Project>

    // see https://docs.gitlab.com/ce/api/projects.html#get-single-project
    @GET(API_VERSION + "/projects/{namespace}%2F{project_name}")
    fun getProject(@Path("namespace") namespace: String,
                   @Path("project_name") projectName: String): Single<Project>

    @GET
    fun getProjects(@Url url: String): Single<Response<List<Project>>>

    @GET(API_VERSION + "/projects")
    fun searchAllProjects(@Query("search") query: String): Single<Response<List<Project>>>

    @GET(API_VERSION + "/projects/{id}/members")
    fun getProjectMembers(@Path("id") projectId: Long): Single<Response<List<User>>>

    @GET
    fun getProjectMembers(@Url url: String): Single<Response<List<User>>>

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/members")
    fun addProjectMember(@Path("id") projectId: Long,
                         @Field("user_id") userId: Long,
                         @Field("access_level") accessLevel: Int): Single<Response<User>>

    @FormUrlEncoded
    @PUT(API_VERSION + "/projects/{id}/members/{user_id}")
    fun editProjectMember(@Path("id") projectId: Long,
                          @Path("user_id") userId: Long,
                          @Field("access_level") accessLevel: Int): Single<User>

    @DELETE(API_VERSION + "/projects/{id}/members/{user_id}")
    fun removeProjectMember(@Path("id") projectId: Long,
                            @Path("user_id") userId: Long): Single<String>

    @POST(API_VERSION + "/projects/{id}/fork")
    fun forkProject(@Path("id") projectId: Long): Single<String>

    @POST(API_VERSION + "/projects/{id}/star")
    fun starProject(@Path("id") projectId: Long): Single<Response<Project>>

    @POST(API_VERSION + "/projects/{id}/unstar")
    fun unstarProject(@Path("id") projectId: Long): Single<Project>

    @Multipart
    @POST(API_VERSION + "/projects/{id}/uploads")
    fun uploadFile(@Path("id") projectId: Long,
                   @Part file: MultipartBody.Part): Single<FileUploadResponse>

    /* --- MILESTONES --- */

    @GET(API_VERSION + "/projects/{id}/milestones")
    fun getMilestones(@Path("id") projectId: Long,
                      @Query("state") state: String): Single<Response<List<Milestone>>>

    @GET
    fun getMilestones(@Url url: String): Single<Response<List<Milestone>>>

    @GET(API_VERSION + "/projects/{id}/issues")
    fun getMilestonesByIid(@Path("id") projectId: Long,
                           @Query("iids") internalMilestoneId: String): Single<List<Milestone>>

    @GET(API_VERSION + "/projects/{id}/milestones/{milestone_id}/issues")
    fun getMilestoneIssues(@Path("id") projectId: Long,
                           @Path("milestone_id") milestoneId: Long): Single<Response<List<Issue>>>

    @GET
    fun getMilestoneIssues(@Url url: String): Single<Response<List<Issue>>>

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/milestones")
    fun createMilestone(@Path("id") projectId: Long,
                        @Field("title") title: String,
                        @Field("description") description: String,
                        @Field("due_date") dueDate: String?): Single<Milestone>

    @FormUrlEncoded
    @PUT(API_VERSION + "/projects/{id}/milestones/{milestone_id}")
    fun editMilestone(@Path("id") projectId: Long,
                      @Path("milestone_id") milestoneId: Long,
                      @Field("title") title: String,
                      @Field("description") description: String,
                      @Field("due_date") dueDate: String?): Single<Milestone>

    @PUT(API_VERSION + "/projects/{id}/milestones/{milestone_id}")
    fun updateMilestoneStatus(@Path("id") projectId: Long,
                              @Path("milestone_id") milestoneId: Long,
                              @Query("state_event") @Milestone.StateEvent status: String): Single<Milestone>

    /* --- MERGE REQUESTS --- */

    @GET(API_VERSION + "/projects/{id}/merge_requests")
    fun getMergeRequests(@Path("id") projectId: Long,
                         @Query("state") state: String): Single<Response<List<MergeRequest>>>

    @GET
    fun getMergeRequests(@Url url: String,
                         @Query("state") state: String): Single<Response<List<MergeRequest>>>

    @GET(API_VERSION + "/projects/{id}/merge_requests")
    fun getMergeRequestsByIid(@Path("id") projectId: Long,
                              @Query("iids") internalMergeRequestId: String): Single<List<MergeRequest>>

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}")
    fun getMergeRequest(@Path("id") projectId: Long,
                        @Path("merge_request_id") mergeRequestId: Long): Single<MergeRequest>

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/commits")
    fun getMergeRequestCommits(@Path("id") projectId: Long,
                               @Path("merge_request_id") mergeRequestId: Long): Single<List<RepositoryCommit>>

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/changes")
    fun getMergeRequestChanges(@Path("id") projectId: Long,
                               @Path("merge_request_id") mergeRequestId: Long): Single<MergeRequest>

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes")
    fun getMergeRequestNotes(@Path("id") projectId: Long,
                             @Path("merge_request_id") mergeRequestId: Long): Single<Response<List<Note>>>

    @GET
    fun getMergeRequestNotes(@Url url: String): Single<Response<List<Note>>>

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes")
    fun addMergeRequestNote(@Path("id") projectId: Long,
                            @Path("merge_request_id") mergeRequestId: Long,
                            @Field("body") body: String): Single<Note>

    @PUT(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/merge")
    fun acceptMergeRequest(@Path("id") projectId: Long,
                           @Path("merge_request_id") mergeRequestId: Long): Single<Response<MergeRequest>>

    /* --- ISSUES --- */

    @GET(API_VERSION + "/projects/{id}/issues")
    fun getIssues(@Path("id") projectId: Long,
                  @Query("state") state: String): Single<Response<List<Issue>>>

    @GET
    fun getIssues(@Url url: String): Single<Response<List<Issue>>>

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}")
    fun getIssue(@Path("id") projectId: Long,
                 @Path("issue_id") issueId: String): Single<Issue>

    @GET(API_VERSION + "/projects/{id}/issues")
    fun getIssuesByIid(@Path("id") projectId: Long): Single<List<Issue>>

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/issues")
    fun createIssue(@Path("id") projectId: Long,
                    @Field("title") title: String,
                    @Field("description") description: String,
                    @Field("assignee_id") assigneeId: Long?,
                    @Field("milestone_id") milestoneId: Long?,
                    @Field("labels") commaSeparatedLabelNames: String?,
                    @Field("confidential") isConfidential: Boolean): Single<Issue>

    @PUT(API_VERSION + "/projects/{id}/issues/{issue_iid}")
    fun updateIssue(@Path("id") projectId: Long,
                    @Path("issue_iid") issueIid: Long,
                    @Query("title") title: String,
                    @Query("description") description: String,
                    @Query("assignee_id") assigneeId: Long?,
                    @Query("milestone_id") milestoneId: Long?,
                    @Query("labels") commaSeparatedLabelNames: String?,
                    @Query("confidential") isConfidential: Boolean): Single<Issue>

    @PUT(API_VERSION + "/projects/{id}/issues/{issue_iid}")
    fun updateIssueStatus(@Path("id") projectId: Long,
                          @Path("issue_iid") issueIid: Long,
                          @Query("state_event") @Issue.EditState status: String): Single<Issue>

    @GET(API_VERSION + "/projects/{id}/issues/{issue_iid}/notes")
    fun getIssueNotes(@Path("id") projectId: Long,
                      @Path("issue_iid") issueIid: Long): Single<Response<List<Note>>>

    @GET
    fun getIssueNotes(@Url url: String): Single<Response<List<Note>>>

    @FormUrlEncoded
    @POST(API_VERSION + "/projects/{id}/issues/{issue_iid}/notes")
    fun addIssueNote(@Path("id") projectId: Long,
                     @Path("issue_iid") issueIid: Long,
                     @Field("body") body: String): Single<Note>

    @DELETE(API_VERSION + "/projects/{id}/issues/{issue_iid}")
    fun deleteIssue(@Path("id") projectId: Long,
                    @Path("issue_iid") issueIid: Long): Single<String>

    /* --- REPOSITORY --- */

    @GET(API_VERSION + "/projects/{id}/repository/branches?order_by=last_activity_at")
    fun getBranches(@Path("id") projectId: Long): Single<List<Branch>>

    @GET(API_VERSION + "/projects/{id}/repository/contributors")
    fun getContributors(@Path("id") projectId: String): Single<List<Contributor>>

    @GET(API_VERSION + "/projects/{id}/repository/tree?per_page=" + TREE_PER_PAGE)
    fun getTree(@Path("id") projectId: Long,
                @Query("ref_name") branchName: String?,
                @Query("path") path: String?): Single<List<RepositoryTreeObject>>

    @GET(API_VERSION + "/projects/{id}/repository/files/{file_path}")
    fun getFile(@Path("id") projectId: Long,
                @Path("file_path") path: String,
                @Query("ref") ref: String): Single<RepositoryFile>

    @GET(API_VERSION + "/projects/{id}/repository/commits")
    fun getCommits(@Path("id") projectId: Long,
                   @Query("ref_name") branchName: String,
                   @Query("page") page: Int): Single<List<RepositoryCommit>>

    @GET(API_VERSION + "/projects/{id}/repository/commits/{sha}")
    fun getCommit(@Path("id") projectId: Long,
                  @Path("sha") commitSHA: String): Single<RepositoryCommit>

    @GET(API_VERSION + "/projects/{id}/repository/commits/{sha}/diff")
    fun getCommitDiff(@Path("id") projectId: Long,
                      @Path("sha") commitSHA: String): Single<List<Diff>>

    /**
     * Get the current labels for a project

     * @param projectId id
     * *
     * @return all the labels within a project
     */
    @GET(API_VERSION + "/projects/{id}/labels")
    fun getLabels(@Path("id") projectId: Long): Single<List<Label>>

    /**
     * Create a new label

     * @param projectId id
     * *
     * @param name      the name of the label
     * *
     * @param color     the color, ex. #ff0000
     * *
     * @return Single
     */
    @POST(API_VERSION + "/projects/{id}/labels")
    fun createLabel(@Path("id") projectId: Long,
                    @Query("name") name: String,
                    @Query("color") color: String?,
                    @Query("description") description: String?): Single<Response<Label>>

    /**
     * Delete the label by its name

     * @param projectId id
     * *
     * @return all the labels within a project
     */
    @DELETE(API_VERSION + "/projects/{id}/labels")
    fun deleteLabel(@Path("id") projectId: Long,
                    @Query("name") name: String): Single<Label>


    /* --- BUILDS --- */
    @GET(API_VERSION + "/projects/{id}/jobs")
    fun getBuilds(@Path("id") projectId: Long,
                  @Query("scope") scope: String?): Single<Response<List<Build>>>

    @GET
    fun getBuilds(@Url url: String,
                  @Query("scope") state: String?): Single<Response<List<Build>>>

    @GET(API_VERSION + "/projects/{id}/jobs/{build_id}")
    fun getBuild(@Path("id") projectId: Long,
                 @Path("build_id") buildId: Long): Single<Build>

    @POST(API_VERSION + "/projects/{id}/jobs/{build_id}/retry")
    fun retryBuild(@Path("id") projectId: Long,
                   @Path("build_id") buildId: Long): Single<Build>

    @POST(API_VERSION + "/projects/{id}/jobs/{build_id}/erase")
    fun eraseBuild(@Path("id") projectId: Long,
                   @Path("build_id") buildId: Long): Single<Build>

    @POST(API_VERSION + "/projects/{id}/jobs/{build_id}/cancel")
    fun cancelBuild(@Path("id") projectId: Long,
                    @Path("build_id") buildId: Long): Single<Build>

    /* --- Pipelines --- */
    @GET(API_VERSION + "/projects/{id}/pipelines")
    fun getPipelines(@Path("id") projectId: Long,
                     @Query("scope") scope: String?): Single<Response<List<Pipeline>>>

    @GET
    fun getPipelines(@Url url: String,
                     @Query("scope") state: String?): Single<Response<List<Pipeline>>>

    @GET(API_VERSION + "/projects/{id}/pipelines/{pipeline_id}/jobs")
    fun getPipelineJobs(@Path("id") projectId: Long, @Path("pipeline_id") pipelineId: Long,
                        @Query("scope") scope: String?): Single<Response<List<Pipeline>>>

    @GET
    fun getPipelineJobs(@Url url: String,
                        @Query("scope") state: String?): Single<Response<List<Pipeline>>>

    @GET(API_VERSION + "/projects/{id}/pipelines/{pipeline_id}")
    fun getPipeline(@Path("id") projectId: Long,
                    @Path("pipeline_id") pipelineId: Long): Single<Pipeline>

    @POST(API_VERSION + "/projects/{id}/pipelines/{pipeline_id}/retry")
    fun retryPipeline(@Path("id") projectId: Long,
                      @Path("pipeline_id") pipelineId: Long): Single<Pipeline>

    @POST(API_VERSION + "/projects/{id}/pipelines/{pipeline_id}/cancel")
    fun cancelPipeline(@Path("id") projectId: Long,
                       @Path("pipeline_id") pipelineId: Long): Single<Pipeline>

    /* --- SNIPPETS --- */
    @GET(API_VERSION + "/projects/{id}/snippets")
    fun getSnippets(@Path("id") projectId: Long): Single<Response<List<Snippet>>>

    @GET
    fun getSnippets(@Url url: String): Single<Response<List<Snippet>>>

    /* --- TODOS --- */
    @GET(API_VERSION + "/todos")
    fun getTodos(@Query("state") @Todo.State state: String): Single<Response<List<Todo>>>

    @GET
    fun getTodosByUrl(@Url url: String): Single<Response<List<Todo>>>

    /* --- TAGS --- */
    @GET(API_VERSION + "/projects/{id}/repository/tags")
    fun getTags(@Path("id") projectId: Long): Single<List<Tag>>

    /* --- AWARD EMOJI --- */
    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}/award_emoji")
    fun getAwardEmojiForIssue(@Path("id") projectId: Long,
                              @Path("issue_id") issueId: String): Single<List<AwardEmoji>>

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/award_emoji")
    fun getAwardEmojiForMergeRequest(@Path("id") projectId: Long,
                                     @Path("merge_request_id") mergeRequestId: String): Single<List<AwardEmoji>>

    @GET(API_VERSION + "/projects/{id}/issues/{issue_id}/notes/{note_id}/award_emoji")
    fun getAwardEmojiForIssueNote(@Path("id") projectId: Long,
                                  @Path("issue_id") issueId: String,
                                  @Path("note_id") noteId: String): Single<List<AwardEmoji>>

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes/{note_id}/award_emoji")
    fun getAwardEmojiForMergeRequestNote(@Path("id") projectId: Long,
                                         @Path("merge_request_id") mergeRequestId: String,
                                         @Path("note_id") noteId: String): Single<List<AwardEmoji>>

    @POST(API_VERSION + "/projects/{id}/issues/{issue_id}/award_emoji")
    fun postAwardEmojiForIssue(@Path("id") projectId: Long,
                               @Path("issue_id") issueId: String): Single<AwardEmoji>

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/award_emoji")
    fun postAwardEmojiForMergeRequest(@Path("id") projectId: Long,
                                      @Path("merge_request_id") mergeRequestId: String): Single<AwardEmoji>

    @GET(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes/{note_id}/award_emoji")
    fun postAwardEmojiForMergeRequestNote(@Path("id") projectId: Long,
                                          @Path("merge_request_id") mergeRequestId: String,
                                          @Path("note_id") noteId: String): Single<AwardEmoji>

    @POST(API_VERSION + "/projects/{id}/issues/{issue_id}/notes/{note_id}/award_emoji")
    fun postAwardEmojiForIssueNote(@Path("id") projectId: Long,
                                   @Path("issue_id") issueId: String,
                                   @Path("note_id") noteId: String): Single<AwardEmoji>

    @DELETE(API_VERSION + "/projects/{id}/issues/{issue_id}/award_emoji/{award_id}")
    fun deleteAwardEmojiForIssue(@Path("id") projectId: Long,
                                 @Path("issue_id") issueId: String,
                                 @Path("award_id") awardId: String): Single<AwardEmoji>

    @DELETE(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/award_emoji/{award_id}")
    fun deleteAwardEmojiForMergeRequest(@Path("id") projectId: Long,
                                        @Path("merge_request_id") mergeRequestId: String,
                                        @Path("award_id") awardId: String): Single<AwardEmoji>

    @DELETE(API_VERSION + "/projects/{id}/issues/{issue_id}/notes/{note_id}/award_emoji/{award_id}")
    fun deleteAwardEmojiForIssueNote(@Path("id") projectId: Long,
                                     @Path("issue_id") issueId: String,
                                     @Path("note_id") noteId: String,
                                     @Path("award_id") awardId: String): Single<AwardEmoji>

    @DELETE(API_VERSION + "/projects/{id}/merge_requests/{merge_request_id}/notes/{note_id}/award_emoji/{award_id}")
    fun deleteAwardEmojiForMergeRequestNote(@Path("id") projectId: Long,
                                            @Path("merge_request_id") mergeRequestId: String,
                                            @Path("note_id") noteId: String,
                                            @Path("award_id") awardId: String): Single<AwardEmoji>

    /* --- MISC --- */
    @GET
    fun getRaw(@Url url: String): Single<String>
}