package com.commit451.gitlab.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.AttachActivity
import com.commit451.gitlab.adapter.NotesAdapter
import com.commit451.gitlab.api.response.FileUploadResponse
import com.commit451.gitlab.event.IssueChangedEvent
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.TransitionFactory
import com.commit451.gitlab.view.SendMessageView
import com.commit451.teleprinter.Teleprinter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_merge_request_discussion.*
import kotlinx.android.synthetic.main.progress_fullscreen.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Shows the discussion of an issue
 */
class IssueDiscussionFragment : BaseFragment() {

    companion object {

        private const val KEY_PROJECT = "project"
        private const val KEY_ISSUE = "issue"

        private const val REQUEST_ATTACH = 1

        fun newInstance(project: Project, issue: Issue): IssueDiscussionFragment {
            val fragment = IssueDiscussionFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, project)
            args.putParcelable(KEY_ISSUE, issue)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var adapter: NotesAdapter
    private lateinit var layoutManagerNotes: LinearLayoutManager
    private lateinit var teleprinter: Teleprinter

    private lateinit var project: Project
    private lateinit var issue: Issue
    private var nextPageUrl: String? = null
    private var loading: Boolean = false

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerNotes.childCount
            val totalItemCount = layoutManagerNotes.itemCount
            val firstVisibleItem = layoutManagerNotes.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMoreNotes()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments?.getParcelable(KEY_PROJECT)!!
        issue = arguments?.getParcelable(KEY_ISSUE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_merge_request_discussion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        teleprinter = Teleprinter(baseActivty)

        adapter = NotesAdapter(project)
        layoutManagerNotes = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        listNotes.layoutManager = layoutManagerNotes
        listNotes.adapter = adapter
        listNotes.addOnScrollListener(onScrollListener)

        sendMessageView.callback = object : SendMessageView.Callback {
            override fun onSendClicked(message: String) {
                postNote(message)
            }

            override fun onAttachmentClicked() {
                val intent = AttachActivity.newIntent(baseActivty, project)
                val activityOptions = TransitionFactory.createFadeInOptions(baseActivty)
                startActivityForResult(intent, REQUEST_ATTACH, activityOptions.toBundle())
            }
        }

        swipeRefreshLayout.setOnRefreshListener { loadNotes() }
        loadNotes()

        App.bus().register(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ATTACH -> {
                if (resultCode == RESULT_OK) {
                    val response = data?.getParcelableExtra<FileUploadResponse>(AttachActivity.KEY_FILE_UPLOAD_RESPONSE)!!
                    progress.visibility = View.GONE
                    sendMessageView.appendText(response.markdown)
                } else {
                    Snackbar.make(root, R.string.failed_to_upload_file, Snackbar.LENGTH_LONG)
                            .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    private fun loadNotes() {
        swipeRefreshLayout.isRefreshing = true
        App.get().gitLab.getIssueNotes(project.id, issue.iid)
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    swipeRefreshLayout.isRefreshing = false
                    loading = false
                    nextPageUrl = it.paginationData.next
                    adapter.setNotes(it.body)
                }, {
                    loading = false
                    Timber.e(it)
                    swipeRefreshLayout.isRefreshing = false
                    Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                            .show()
                })
    }

    fun loadMoreNotes() {
        adapter.setLoading(true)
        App.get().gitLab.getIssueNotes(nextPageUrl!!.toString())
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    adapter.setLoading(false)
                    loading = false
                    nextPageUrl = it.paginationData.next
                    adapter.addNotes(it.body)
                }, {
                    loading = false
                    Timber.e(it)
                    adapter.setLoading(false)
                    Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                            .show()
                })
    }

    fun postNote(message: String) {

        if (message.isBlank()) {
            return
        }

        progress.visibility = View.VISIBLE
        progress.alpha = 0.0f
        progress.animate().alpha(1.0f)
        // Clear text & collapse keyboard
        teleprinter.hideKeyboard()
        sendMessageView.clearText()

        App.get().gitLab.addIssueNote(project.id, issue.iid, message)
                .with(this)
                .subscribe({
                    progress.visibility = View.GONE
                    adapter.addNote(it)
                    listNotes.smoothScrollToPosition(0)
                }, {
                    Timber.e(it)
                    progress.visibility = View.GONE
                    Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                            .show()
                })
    }

    @Subscribe
    fun onEvent(event: IssueChangedEvent) {
        if (issue.iid == event.issue.iid) {
            issue = event.issue
            loadNotes()
        }
    }
}
