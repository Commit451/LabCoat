package com.commit451.gitlab.api

import com.commit451.gitlab.api.response.FileUploadResponse
import com.commit451.gitlab.model.api.*
import com.commit451.gitlab.model.api.Tag
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Defines the interface for Retrofit for the GitLabService API
 * http://doc.gitlab.com/ce/api/README.html
 */
interface GitLabService {

    companion object {
        const val API_VERSION = "api/v4"
        const val MAX_PER_PAGE = "100"
    }

    /* --- USERS --- */

    /**
     * Get currently authenticated user
     */
    @GET("user")
    fun getThisUser(): Single<Response<User>>

    @GET("users")
    fun searchUsers(@Query("search") query: String): Single<Response<List<User>>>

    /* --- GROUPS --- */
    @GET("groups")
    fun getGroups(): Single<Response<List<Group>>>

    @GET("groups/{id}")
    fun getGroup(@Path("id") id: Long): Single<Group>

    @GET("groups/{id}/projects?order_by=last_activity_at")
    fun getGroupProjects(@Path("id") id: Long): Single<Response<List<Project>>>

    @GET("groups/{id}/members")
    fun getGroupMembers(@Path("id") groupId: Long): Single<Response<List<User>>>

    @FormUrlEncoded
    @POST("groups/{id}/members")
    fun addGroupMember(@Path("id") groupId: Long,
                       @Field("user_id") userId: Long,
                       @Field("access_level") accessLevel: Int): Single<Response<User>>

    @FormUrlEncoded
    @PUT("groups/{id}/members/{user_id}")
    fun editGroupMember(@Path("id") groupId: Long,
                        @Path("user_id") userId: Long,
                        @Field("access_level") accessLevel: Int): Single<User>

    @DELETE("groups/{id}/members/{user_id}")
    fun removeGroupMember(@Path("id") groupId: Long,
                          @Path("user_id") userId: Long): Completable

    /* --- PROJECTS --- */

    @GET("projects?membership=true&order_by=last_activity_at")
    fun getAllProjects(): Single<Response<List<Project>>>

    @GET("users/{userId}/projects?order_by=last_activity_at")
    fun getMyProjects(@Path("userId") userId: String): Single<Response<List<Project>>>

    @GET("projects?starred=true")
    fun getStarredProjects(): Single<Response<List<Project>>>

    @GET("projects/{id}")
    fun getProject(@Path("id") projectId: String): Single<Project>

    // see https://docs.gitlab.com/ce/api/projects.html#get-single-project
    @GET("projects/{namespace}%2F{project_name}")
    fun getProject(@Path("namespace") namespace: String,
                   @Path("project_name") projectName: String): Single<Project>

    @GET("projects")
    fun searchAllProjects(@Query("search") query: String): Single<Response<List<Project>>>

    @GET("projects/{id}/members")
    fun getProjectMembers(@Path("id") projectId: Long): Single<Response<List<User>>>

    @GET
    fun getProjectMembers(@Url url: String): Single<Response<List<User>>>

    @FormUrlEncoded
    @POST("projects/{id}/members")
    fun addProjectMember(@Path("id") projectId: Long,
                         @Field("user_id") userId: Long,
                         @Field("access_level") accessLevel: Int): Single<Response<User>>

    @FormUrlEncoded
    @PUT("projects/{id}/members/{user_id}")
    fun editProjectMember(@Path("id") projectId: Long,
                          @Path("user_id") userId: Long,
                          @Field("access_level") accessLevel: Int): Single<User>

    @DELETE("projects/{id}/members/{user_id}")
    fun removeProjectMember(@Path("id") projectId: Long,
                            @Path("user_id") userId: Long): Completable

    @POST("projects/{id}/fork")
    fun forkProject(@Path("id") projectId: Long): Completable

    @POST("projects/{id}/star")
    fun starProject(@Path("id") projectId: Long): Single<Response<Project>>

    @POST("projects/{id}/unstar")
    fun unstarProject(@Path("id") projectId: Long): Single<Project>

    @Multipart
    @POST("projects/{id}/uploads")
    fun uploadFile(@Path("id") projectId: Long,
                   @Part file: MultipartBody.Part): Single<FileUploadResponse>

    /* --- MILESTONES --- */

    @GET("projects/{id}/milestones")
    fun getMilestones(@Path("id") projectId: Long,
                      @Query("state") state: String?): Single<Response<List<Milestone>>>

    @GET
    fun getMilestones(@Url url: String): Single<Response<List<Milestone>>>

    @GET("projects/{id}/issues")
    fun getMilestonesByIid(@Path("id") projectId: Long,
                           @Query("iids") internalMilestoneId: String): Single<List<Milestone>>

    @GET("projects/{id}/milestones/{milestone_id}/issues")
    fun getMilestoneIssues(@Path("id") projectId: Long,
                           @Path("milestone_id") milestoneId: Long): Single<Response<List<Issue>>>

    @GET
    fun getMilestoneIssues(@Url url: String): Single<Response<List<Issue>>>

    @FormUrlEncoded
    @POST("projects/{id}/milestones")
    fun createMilestone(@Path("id") projectId: Long,
                        @Field("title") title: String,
                        @Field("description") description: String,
                        @Field("due_date") dueDate: String?): Single<Milestone>

    @FormUrlEncoded
    @PUT("projects/{id}/milestones/{milestone_id}")
    fun editMilestone(@Path("id") projectId: Long,
                      @Path("milestone_id") milestoneId: Long,
                      @Field("title") title: String,
                      @Field("description") description: String,
                      @Field("due_date") dueDate: String?): Single<Milestone>

    @PUT("projects/{id}/milestones/{milestone_id}")
    fun updateMilestoneStatus(@Path("id") projectId: Long,
                              @Path("milestone_id") milestoneId: Long,
                              @Query("state_event") @Milestone.StateEvent status: String): Single<Milestone>

    /* --- MERGE REQUESTS --- */

    @GET("projects/{id}/merge_requests")
    fun getMergeRequests(@Path("id") projectId: Long,
                         @Query("state") state: String): Single<Response<List<MergeRequest>>>

    @GET("projects/{id}/merge_requests")
    fun getMergeRequestsByIid(@Path("id") projectId: Long,
                              @Query("iids") internalMergeRequestId: String): Single<List<MergeRequest>>

    @GET("projects/{id}/merge_requests/{merge_request_id}")
    fun getMergeRequest(@Path("id") projectId: Long,
                        @Path("merge_request_id") mergeRequestId: Long): Single<MergeRequest>

    @GET("projects/{id}/merge_requests/{merge_request_id}/commits")
    fun getMergeRequestCommits(@Path("id") projectId: Long,
                               @Path("merge_request_id") mergeRequestId: Long): Single<Response<List<RepositoryCommit>>>

    @GET("projects/{id}/merge_requests/{merge_request_id}/changes")
    fun getMergeRequestChanges(@Path("id") projectId: Long,
                               @Path("merge_request_id") mergeRequestId: Long): Single<MergeRequest>

    @GET("projects/{id}/merge_requests/{merge_request_id}/notes")
    fun getMergeRequestNotes(@Path("id") projectId: Long,
                             @Path("merge_request_id") mergeRequestId: Long): Single<Response<List<Note>>>

    @FormUrlEncoded
    @POST("projects/{id}/merge_requests/{merge_request_id}/notes")
    fun addMergeRequestNote(@Path("id") projectId: Long,
                            @Path("merge_request_id") mergeRequestId: Long,
                            @Field("body") body: String): Single<Note>

    @PUT("projects/{id}/merge_requests/{merge_request_id}/merge")
    fun acceptMergeRequest(@Path("id") projectId: Long,
                           @Path("merge_request_id") mergeRequestId: Long): Single<Response<MergeRequest>>

    /* --- ISSUES --- */

    @GET("projects/{id}/issues")
    fun getIssues(@Path("id") projectId: Long,
                  @Query("state") state: String): Single<Response<List<Issue>>>

    @GET
    fun getIssues(@Url url: String): Single<Response<List<Issue>>>

    @GET("projects/{id}/issues/{issue_id}")
    fun getIssue(@Path("id") projectId: Long,
                 @Path("issue_id") issueId: String): Single<Issue>

    @FormUrlEncoded
    @POST("projects/{id}/issues")
    fun createIssue(@Path("id") projectId: Long,
                    @Field("title") title: String,
                    @Field("description") description: String,
                    @Field("assignee_id") assigneeId: Long?,
                    @Field("milestone_id") milestoneId: Long?,
                    @Field("labels") commaSeparatedLabelNames: String?,
                    @Field("confidential") isConfidential: Boolean): Single<Issue>

    @PUT("projects/{id}/issues/{issue_iid}")
    fun updateIssue(@Path("id") projectId: Long,
                    @Path("issue_iid") issueIid: Long,
                    @Query("title") title: String,
                    @Query("description") description: String,
                    @Query("assignee_id") assigneeId: Long?,
                    @Query("milestone_id") milestoneId: Long?,
                    @Query("labels") commaSeparatedLabelNames: String?,
                    @Query("confidential") isConfidential: Boolean): Single<Issue>

    @PUT("projects/{id}/issues/{issue_iid}")
    fun updateIssueStatus(@Path("id") projectId: Long,
                          @Path("issue_iid") issueIid: Long,
                          @Query("state_event") @Issue.EditState status: String): Single<Issue>

    @GET("projects/{id}/issues/{issue_iid}/notes")
    fun getIssueNotes(@Path("id") projectId: Long,
                      @Path("issue_iid") issueIid: Long): Single<Response<List<Note>>>

    @FormUrlEncoded
    @POST("projects/{id}/issues/{issue_iid}/notes")
    fun addIssueNote(@Path("id") projectId: Long,
                     @Path("issue_iid") issueIid: Long,
                     @Field("body") body: String): Single<Note>

    @DELETE("projects/{id}/issues/{issue_iid}")
    fun deleteIssue(@Path("id") projectId: Long,
                    @Path("issue_iid") issueIid: Long): Completable

    /* --- REPOSITORY --- */

    @GET("projects/{id}/repository/branches?order_by=last_activity_at")
    fun getBranches(@Path("id") projectId: Long): Single<Response<List<Branch>>>

    @GET
    fun getBranches(@Url url: String): Single<Response<List<Branch>>>

    @GET("projects/{id}/repository/contributors")
    fun getContributors(@Path("id") projectId: String): Single<List<Contributor>>

    @GET("projects/{id}/repository/tree?per_page=$MAX_PER_PAGE")
    fun getTree(@Path("id") projectId: Long,
                @Query("ref") ref: String?,
                @Query("path") path: String?): Single<List<RepositoryTreeObject>>

    @GET("projects/{id}/repository/files/{file_path}")
    fun getFile(@Path("id") projectId: Long,
                @Path("file_path") path: String,
                @Query("ref") ref: String): Single<RepositoryFile>

    @GET("projects/{id}/repository/commits")
    fun getCommits(@Path("id") projectId: Long,
                   @Query("ref_name") branchName: String): Single<Response<List<RepositoryCommit>>>

    @GET("projects/{id}/repository/commits/{sha}")
    fun getCommit(@Path("id") projectId: Long,
                  @Path("sha") commitSHA: String): Single<RepositoryCommit>

    @GET("projects/{id}/repository/commits/{sha}/diff")
    fun getCommitDiff(@Path("id") projectId: Long,
                      @Path("sha") commitSHA: String): Single<List<Diff>>

    /**
     * Get the current labels for a project

     * @param projectId id
     * *
     * @return all the labels within a project
     */
    @GET("projects/{id}/labels?per_page=$MAX_PER_PAGE")
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
    @POST("projects/{id}/labels")
    fun createLabel(@Path("id") projectId: Long,
                    @Query("name") name: String,
                    @Query("color") color: String?,
                    @Query("description") description: String?): Single<Response<Label>>


    /* --- BUILDS --- */
    @GET("projects/{id}/jobs")
    fun getBuilds(@Path("id") projectId: Long,
                  @Query("scope") scope: String?): Single<Response<List<Build>>>

    @GET
    fun getBuilds(@Url url: String,
                  @Query("scope") state: String?): Single<Response<List<Build>>>

    @GET("projects/{id}/jobs/{build_id}")
    fun getBuild(@Path("id") projectId: Long,
                 @Path("build_id") buildId: Long): Single<Build>

    @POST("projects/{id}/jobs/{build_id}/retry")
    fun retryBuild(@Path("id") projectId: Long,
                   @Path("build_id") buildId: Long): Single<Build>

    @POST("projects/{id}/jobs/{build_id}/erase")
    fun eraseBuild(@Path("id") projectId: Long,
                   @Path("build_id") buildId: Long): Single<Build>

    @POST("projects/{id}/jobs/{build_id}/cancel")
    fun cancelBuild(@Path("id") projectId: Long,
                    @Path("build_id") buildId: Long): Single<Build>

    /* --- Pipelines --- */
    @GET("projects/{id}/pipelines")
    fun getPipelines(@Path("id") projectId: Long,
                     @Query("scope") scope: String?): Single<Response<List<Pipeline>>>

    @GET("projects/{id}/pipelines/{pipeline_id}")
    fun getPipeline(@Path("id") projectId: Long,
                    @Path("pipeline_id") pipelineId: Long): Single<Pipeline>

    @POST("projects/{id}/pipelines/{pipeline_id}/retry")
    fun retryPipeline(@Path("id") projectId: Long,
                      @Path("pipeline_id") pipelineId: Long): Single<Pipeline>

    @POST("projects/{id}/pipelines/{pipeline_id}/cancel")
    fun cancelPipeline(@Path("id") projectId: Long,
                       @Path("pipeline_id") pipelineId: Long): Single<Pipeline>

    /* --- SNIPPETS --- */
    @GET("projects/{id}/snippets")
    fun getSnippets(@Path("id") projectId: Long): Single<Response<List<Snippet>>>

    /* --- TODOS --- */
    @GET("todos")
    fun getTodos(@Query("state") @Todo.State state: String): Single<Response<List<Todo>>>

    /* --- TAGS --- */
    @GET("projects/{id}/repository/tags")
    fun getTags(@Path("id") projectId: Long): Single<List<Tag>>

    /* --- MISC --- */
    @GET
    fun getRaw(@Url url: String): Single<String>

    @GET
    fun get(@Url url: String): Single<Response<ResponseBody>>
}
