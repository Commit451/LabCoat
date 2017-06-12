package com.commit451.gitlab.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.IssueDetailsAdapter
import com.commit451.gitlab.event.IssueChangedEvent
import com.commit451.gitlab.event.IssueReloadEvent
import com.commit451.gitlab.extension.getParcelerParcelable
import com.commit451.gitlab.extension.getUrl
import com.commit451.gitlab.extension.putParcelParcelableExtra
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.model.api.FileUploadResponse
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Note
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.IntentUtil
import com.commit451.gitlab.util.LinkHeaderParser
import com.commit451.gitlab.view.SendMessageView
import com.commit451.teleprinter.Teleprinter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.Subscribe
import retrofit2.Response
import timber.log.Timber

/**
 * Shows off an issue like a bar of gold
 */
class IssueActivity : BaseActivity() {

    companion object {

        private val EXTRA_PROJECT = "extra_project"
        private val EXTRA_SELECTED_ISSUE = "extra_selected_issue"
        private val EXTRA_PROJECT_NAMESPACE = "project_namespace"
        private val EXTRA_PROJECT_NAME = "project_name"
        private val EXTRA_ISSUE_IID = "extra_issue_iid"

        private val REQUEST_ATTACH = 1

        fun newIntent(context: Context, project: Project, issue: Issue): Intent {
            val intent = Intent(context, IssueActivity::class.java)
            intent.putParcelParcelableExtra(EXTRA_PROJECT, project)
            intent.putParcelParcelableExtra(EXTRA_SELECTED_ISSUE, issue)
            return intent
        }

        fun newIntent(context: Context, namespace: String, projectName: String, issueIid: String): Intent {
            val intent = Intent(context, IssueActivity::class.java)
            intent.putExtra(EXTRA_PROJECT_NAMESPACE, namespace)
            intent.putExtra(EXTRA_PROJECT_NAME, projectName)
            intent.putExtra(EXTRA_ISSUE_IID, issueIid)
            return intent
        }
    }

    @BindView(R.id.root) lateinit var root: ViewGroup
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.issue_title) lateinit var textTitle: TextView
    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list) lateinit var listNotes: RecyclerView
    @BindView(R.id.send_message_view) lateinit var sendMessageView: SendMessageView
    @BindView(R.id.progress) lateinit var progress: View
    @BindView(R.id.toolbar_title) lateinit var toolbarTitle: TextView
    @BindView(R.id.toolbar_subtitle) lateinit var toolbarSubTitle: TextView

    lateinit var menuItemOpenClose: MenuItem
    lateinit var adapterIssueDetails: IssueDetailsAdapter
    lateinit var layoutManagerNotes: LinearLayoutManager
    lateinit var teleprinter: Teleprinter

    var project: Project? = null
    var issue: Issue? = null
    var issueIid: String? = null
    var loading: Boolean = false
    var nextPageUrl: Uri? = null

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerNotes.childCount
            val totalItemCount = layoutManagerNotes.itemCount
            val firstVisibleItem = layoutManagerNotes.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMoreNotes()
            }
        }
    }

    val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.action_share -> {
                IntentUtil.share(root, issue!!.getUrl(project!!))
                return@OnMenuItemClickListener true
            }
            R.id.action_close -> {
                closeOrOpenIssue()
                return@OnMenuItemClickListener true
            }
            R.id.action_delete -> {
                App.get().gitLab.deleteIssue(project!!.id, issue!!.iid)
                        .setup(bindToLifecycle())
                        .subscribe(object : CustomSingleObserver<String>() {

                            override fun error(t: Throwable) {
                                Timber.e(t)
                                Snackbar.make(root, getString(R.string.failed_to_delete_issue), Snackbar.LENGTH_SHORT)
                                        .show()
                            }

                            override fun success(s: String) {
                                App.bus().post(IssueReloadEvent())
                                Toast.makeText(this@IssueActivity, R.string.issue_deleted, Toast.LENGTH_SHORT)
                                        .show()
                                finish()
                            }
                        })
                return@OnMenuItemClickListener true
            }
        }
        false
    }

    @OnClick(R.id.fab_edit_issue)
    fun onEditIssueClick(fab: View) {
        Navigator.navigateToEditIssue(this@IssueActivity, fab, project!!, issue!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue)
        ButterKnife.bind(this)
        teleprinter = Teleprinter(this)
        App.bus().register(this)

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.inflateMenu(R.menu.share)
        toolbar.inflateMenu(R.menu.close)
        toolbar.inflateMenu(R.menu.delete)
        menuItemOpenClose = toolbar.menu.findItem(R.id.action_close)
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener)

        layoutManagerNotes = LinearLayoutManager(this)
        listNotes.layoutManager = layoutManagerNotes
        listNotes.addOnScrollListener(onScrollListener)

        sendMessageView.callback = object : SendMessageView.Callback {
            override fun onSendClicked(message: String) {
                postNote(message)
            }

            override fun onAttachmentClicked() {
                Navigator.navigateToAttach(this@IssueActivity, project!!, REQUEST_ATTACH)
            }
        }

        swipeRefreshLayout.setOnRefreshListener { loadNotes() }

        if (intent.hasExtra(EXTRA_SELECTED_ISSUE)) {
            project = intent.getParcelerParcelable<Project>(EXTRA_PROJECT)
            issue = intent.getParcelerParcelable<Issue>(EXTRA_SELECTED_ISSUE)
            adapterIssueDetails = IssueDetailsAdapter(this@IssueActivity, issue, project!!)
            listNotes.adapter = adapterIssueDetails
            bindIssue()
            bindProject()
            loadNotes()
        } else if (intent.hasExtra(EXTRA_ISSUE_IID)) {
            issueIid = intent.getStringExtra(EXTRA_ISSUE_IID)
            val projectNamespace = intent.getStringExtra(EXTRA_PROJECT_NAMESPACE)
            val projectName = intent.getStringExtra(EXTRA_PROJECT_NAME)
            swipeRefreshLayout.isRefreshing = true
            App.get().gitLab.getProject(projectNamespace, projectName)
                    .flatMap { project ->
                        this@IssueActivity.project = project
                        App.get().gitLab.getIssuesByIid(project.id)
                    }
                    .compose(this.bindToLifecycle<List<Issue>>())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : CustomSingleObserver<List<Issue>>() {

                        override fun error(t: Throwable) {
                            Timber.e(t)
                            swipeRefreshLayout.isRefreshing = false
                            Snackbar.make(root, getString(R.string.failed_to_load), Snackbar.LENGTH_SHORT)
                                    .show()
                        }

                        override fun success(issues: List<Issue>) {
                            if (issues.isEmpty()) {
                                swipeRefreshLayout.isRefreshing = false
                                Snackbar.make(root, getString(R.string.failed_to_load), Snackbar.LENGTH_SHORT)
                                        .show()
                            } else {
                                issue = issues[0]
                                adapterIssueDetails = IssueDetailsAdapter(this@IssueActivity, issue, project!!)
                                listNotes.adapter = adapterIssueDetails
                                bindIssue()
                                bindProject()
                                loadNotes()
                            }
                        }
                    })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ATTACH ->
                if (resultCode == Activity.RESULT_OK) {
                    val response = data?.getParcelerParcelable<FileUploadResponse>(AttachActivity.KEY_FILE_UPLOAD_RESPONSE)!!
                    progress.visibility = View.GONE
                    sendMessageView.appendText(response.markdown)
                } else {
                    Snackbar.make(root, R.string.failed_to_upload_file, Snackbar.LENGTH_LONG)
                            .show()
                }
        }
    }

    override fun onDestroy() {
        App.bus().unregister(this)
        super.onDestroy()
    }

    fun bindProject() {
        toolbarSubTitle.text = project?.nameWithNamespace
    }

    fun bindIssue() {
        setOpenCloseMenuStatus()
        textTitle.text = issue?.title
        toolbarTitle.text = getString(R.string.issue_number, issue?.iid)
        if (issue?.isConfidential!!) {
            toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_confidential_24dp, 0)
        }
        adapterIssueDetails.updateIssue(issue!!)
    }

    fun loadNotes() {
        swipeRefreshLayout.isRefreshing = true
        loading = true
        App.get().gitLab.getIssueNotes(project!!.id, issue!!.iid)
                .compose(this.bindToLifecycle<Response<List<Note>>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<List<Note>>() {

                    override fun error(t: Throwable) {
                        loading = false
                        Timber.e(t)
                        swipeRefreshLayout.isRefreshing = false
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun responseNonNullSuccess(notes: List<Note>) {
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapterIssueDetails.setNotes(notes)
                    }
                })
    }

    fun loadMoreNotes() {
        loading = true
        adapterIssueDetails.setLoading(true)
        App.get().gitLab.getIssueNotes(nextPageUrl!!.toString())
                .compose(this.bindToLifecycle<Response<List<Note>>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<List<Note>>() {

                    override fun error(t: Throwable) {
                        loading = false
                        Timber.e(t)
                        adapterIssueDetails.setLoading(false)
                    }

                    override fun responseNonNullSuccess(notes: List<Note>) {
                        loading = false
                        adapterIssueDetails.setLoading(false)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapterIssueDetails.addNotes(notes)
                    }
                })
    }

    fun postNote(message: String) {

        if (message.isEmpty()) {
            return
        }

        progress.visibility = View.VISIBLE
        progress.alpha = 0.0f
        progress.animate().alpha(1.0f)
        // Clear text & collapse keyboard
        teleprinter.hideKeyboard()
        sendMessageView.clearText()

        App.get().gitLab.addIssueNote(project!!.id, issue!!.iid, message)
                .setup(bindToLifecycle())
                .subscribe(object : CustomSingleObserver<Note>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        progress.visibility = View.GONE
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun success(note: Note) {
                        progress.visibility = View.GONE
                        adapterIssueDetails.addNote(note)
                        listNotes.smoothScrollToPosition(IssueDetailsAdapter.headerCount)
                    }
                })
    }

    fun closeOrOpenIssue() {
        progress.visibility = View.VISIBLE
        if (issue!!.state == Issue.STATE_CLOSED) {
            updateIssueStatus(App.get().gitLab.updateIssueStatus(project!!.id, issue!!.iid, Issue.STATE_REOPEN))
        } else {
            updateIssueStatus(App.get().gitLab.updateIssueStatus(project!!.id, issue!!.iid, Issue.STATE_CLOSE))
        }
    }

    fun updateIssueStatus(observable: Single<Issue>) {
        observable
                .setup(bindToLifecycle())
                .subscribe(object : CustomSingleObserver<Issue>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        progress.visibility = View.GONE
                        Snackbar.make(root, getString(R.string.error_changing_issue), Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun success(issue: Issue) {
                        progress.visibility = View.GONE
                        this@IssueActivity.issue = issue
                        App.bus().post(IssueChangedEvent(this@IssueActivity.issue!!))
                        App.bus().post(IssueReloadEvent())
                        setOpenCloseMenuStatus()
                        loadNotes()
                    }
                })
    }

    fun setOpenCloseMenuStatus() {
        menuItemOpenClose.setTitle(if (issue!!.state == Issue.STATE_CLOSED) R.string.reopen else R.string.close)
    }

    @Subscribe
    fun onEvent(event: IssueChangedEvent) {
        if (issue!!.id == event.issue.id) {
            issue = event.issue
            bindIssue()
            loadNotes()
        }
    }
}
