package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.*
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomSingleObserver
import timber.log.Timber


/**
 * Intermediate activity when deep linking to another activity and things need to load
 */
class LoadSomeInfoActivity : BaseActivity() {

    companion object {

        private val EXTRA_LOAD_TYPE = "load_type"
        private val EXTRA_PROJECT_NAMESPACE = "project_namespace"
        private val EXTRA_PROJECT_NAME = "project_name"
        private val EXTRA_COMMIT_SHA = "extra_commit_sha"
        private val EXTRA_MERGE_REQUEST = "merge_request"
        private val EXTRA_BUILD_ID = "build_id"
        private val EXTRA_MILESTONE_ID = "milestone_id"
        private val EXTRA_ISSUE_ID = "issue_id"

        private val LOAD_TYPE_DIFF = 0
        private val LOAD_TYPE_MERGE_REQUEST = 1
        private val LOAD_TYPE_BUILD = 2
        private val LOAD_TYPE_MILESTONE = 3
        private val LOAD_TYPE_ISSUE = 4

        fun newIntent(context: Context, namespace: String, projectName: String, commitSha: String): Intent {
            val intent = Intent(context, LoadSomeInfoActivity::class.java)
            intent.putExtra(EXTRA_PROJECT_NAMESPACE, namespace)
            intent.putExtra(EXTRA_PROJECT_NAME, projectName)
            intent.putExtra(EXTRA_COMMIT_SHA, commitSha)
            intent.putExtra(EXTRA_LOAD_TYPE, LOAD_TYPE_DIFF)
            return intent
        }

        fun newIssueIntent(context: Context, namespace: String, projectName: String, issueId: String): Intent {
            val intent = Intent(context, LoadSomeInfoActivity::class.java)
            intent.putExtra(EXTRA_PROJECT_NAMESPACE, namespace)
            intent.putExtra(EXTRA_PROJECT_NAME, projectName)
            intent.putExtra(EXTRA_ISSUE_ID, issueId)
            intent.putExtra(EXTRA_LOAD_TYPE, LOAD_TYPE_ISSUE)
            return intent
        }

        fun newMergeRequestIntent(context: Context, namespace: String, projectName: String, mergeRequestId: String): Intent {
            val intent = Intent(context, LoadSomeInfoActivity::class.java)
            intent.putExtra(EXTRA_PROJECT_NAMESPACE, namespace)
            intent.putExtra(EXTRA_PROJECT_NAME, projectName)
            intent.putExtra(EXTRA_MERGE_REQUEST, mergeRequestId)
            intent.putExtra(EXTRA_LOAD_TYPE, LOAD_TYPE_MERGE_REQUEST)
            return intent
        }

        fun newBuildIntent(context: Context, namespace: String, projectName: String, buildId: Long): Intent {
            val intent = Intent(context, LoadSomeInfoActivity::class.java)
            intent.putExtra(EXTRA_PROJECT_NAMESPACE, namespace)
            intent.putExtra(EXTRA_PROJECT_NAME, projectName)
            intent.putExtra(EXTRA_BUILD_ID, buildId)
            intent.putExtra(EXTRA_LOAD_TYPE, LOAD_TYPE_BUILD)
            return intent
        }

        fun newMilestoneIntent(context: Context, namespace: String, projectName: String, milestoneIid: String): Intent {
            val intent = Intent(context, LoadSomeInfoActivity::class.java)
            intent.putExtra(EXTRA_PROJECT_NAMESPACE, namespace)
            intent.putExtra(EXTRA_PROJECT_NAME, projectName)
            intent.putExtra(EXTRA_MILESTONE_ID, milestoneIid)
            intent.putExtra(EXTRA_LOAD_TYPE, LOAD_TYPE_MILESTONE)
            return intent
        }
    }

    @BindView(R.id.progress) lateinit var progress: View

    private var loadType: Int = 0

    private var project: Project? = null

    @OnClick(R.id.root)
    fun onRootClicked() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        ButterKnife.bind(this)
        progress.visibility = View.VISIBLE
        loadType = intent.getIntExtra(EXTRA_LOAD_TYPE, -1)
        Timber.d("Loading some info type: %d", loadType)

        when (loadType) {
            LOAD_TYPE_DIFF, LOAD_TYPE_MERGE_REQUEST, LOAD_TYPE_BUILD, LOAD_TYPE_MILESTONE, LOAD_TYPE_ISSUE -> {
                val namespace = intent.getStringExtra(EXTRA_PROJECT_NAMESPACE)
                val project = intent.getStringExtra(EXTRA_PROJECT_NAME)
                App.get().gitLab.getProject(namespace, project)
                        .with(this)
                        .subscribe(object : CustomSingleObserver<Project>() {

                            override fun error(t: Throwable) {
                                Timber.e(t)
                                this@LoadSomeInfoActivity.onError()
                            }

                            override fun success(project: Project) {
                                loadNextPart(project)
                            }
                        })
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.do_nothing, R.anim.fade_out)
    }

    fun loadNextPart(response: Project) {
        project = response
        when (loadType) {
            LOAD_TYPE_ISSUE -> {
                val issueId = intent.getStringExtra(EXTRA_ISSUE_ID)
                App.get().gitLab.getIssue(response.id, issueId)
                        .with(this)
                        .subscribe(object : CustomSingleObserver<Issue>() {

                            override fun error(t: Throwable) {
                                Timber.e(t)
                                this@LoadSomeInfoActivity.onError()
                            }

                            override fun success(issue: Issue) {
                                Navigator.navigateToIssue(this@LoadSomeInfoActivity, project!!, issue)
                                finish()
                            }
                        })
                return
            }
            LOAD_TYPE_DIFF -> {
                val sha = intent.getStringExtra(EXTRA_COMMIT_SHA)
                App.get().gitLab.getCommit(response.id, sha)
                        .with(this)
                        .subscribe(object : CustomSingleObserver<RepositoryCommit>() {

                            override fun error(t: Throwable) {
                                Timber.e(t)
                                this@LoadSomeInfoActivity.onError()
                            }

                            override fun success(repositoryCommit: RepositoryCommit) {
                                Navigator.navigateToDiffActivity(this@LoadSomeInfoActivity, project!!, repositoryCommit)
                                finish()
                            }
                        })
                return
            }
            LOAD_TYPE_MERGE_REQUEST -> {
                val mergeRequestId = intent.getStringExtra(EXTRA_MERGE_REQUEST)
                App.get().gitLab.getMergeRequestsByIid(response.id, mergeRequestId)
                        .with(this)
                        .subscribe(object : CustomSingleObserver<List<MergeRequest>>() {

                            override fun error(t: Throwable) {
                                Timber.e(t)
                                this@LoadSomeInfoActivity.onError()
                            }

                            override fun success(mergeRequests: List<MergeRequest>) {
                                if (!mergeRequests.isEmpty()) {
                                    Navigator.navigateToMergeRequest(this@LoadSomeInfoActivity, project!!, mergeRequests[0])
                                    finish()
                                } else {
                                    this@LoadSomeInfoActivity.onError()
                                }
                            }
                        })
                return
            }
            LOAD_TYPE_BUILD -> {
                val buildId = intent.getLongExtra(EXTRA_BUILD_ID, -1)
                App.get().gitLab.getBuild(response.id, buildId)
                        .with(this)
                        .subscribe(object : CustomSingleObserver<Build>() {

                            override fun error(t: Throwable) {
                                Timber.e(t)
                                this@LoadSomeInfoActivity.onError()
                            }

                            override fun success(build: Build) {
                                Navigator.navigateToBuild(this@LoadSomeInfoActivity, project!!, build)
                                finish()
                            }
                        })
                return
            }
            LOAD_TYPE_MILESTONE -> {
                val milestoneId = intent.getStringExtra(EXTRA_MILESTONE_ID)
                App.get().gitLab.getMilestonesByIid(response.id, milestoneId)
                        .with(this)
                        .subscribe(object : CustomSingleObserver<List<Milestone>>() {

                            override fun error(t: Throwable) {
                                Timber.e(t)
                                this@LoadSomeInfoActivity.onError()
                            }

                            override fun success(milestones: List<Milestone>) {
                                if (!milestones.isEmpty()) {
                                    Navigator.navigateToMilestone(this@LoadSomeInfoActivity, project!!, milestones[0])
                                    finish()
                                } else {
                                    this@LoadSomeInfoActivity.onError()
                                }
                            }
                        })
                return
            }
        }

    }

    fun onError() {
        Toast.makeText(this@LoadSomeInfoActivity, R.string.failed_to_load, Toast.LENGTH_SHORT)
                .show()
        finish()
    }
}
