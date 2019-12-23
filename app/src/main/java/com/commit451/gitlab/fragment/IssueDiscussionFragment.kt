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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.AttachActivity
import com.commit451.gitlab.adapter.NotesAdapter
import com.commit451.gitlab.api.response.FileUploadResponse
import com.commit451.gitlab.event.IssueChangedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Note
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.TransitionFactory
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import com.commit451.gitlab.view.SendMessageView
import com.commit451.teleprinter.Teleprinter
import com.google.android.material.snackbar.Snackbar
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Shows the discussion of an issue
 */
class IssueDiscussionFragment : ButterKnifeFragment() {

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

    @BindView(R.id.root)
    lateinit var root: ViewGroup
    @BindView(R.id.swipe_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list)
    lateinit var listNotes: RecyclerView
    @BindView(R.id.send_message_view)
    lateinit var sendMessageView: SendMessageView
    @BindView(R.id.progress)
    lateinit var progress: View

    lateinit var adapter: NotesAdapter
    lateinit var layoutManagerNotes: LinearLayoutManager
    lateinit var teleprinter: Teleprinter

    lateinit var project: Project
    lateinit var issue: Issue
    var nextPageUrl: Uri? = null
    var loading: Boolean = false

    val onScrollListener = object : RecyclerView.OnScrollListener() {
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
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Note>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun responseNonNullSuccess(notes: List<Note>) {
                        swipeRefreshLayout.isRefreshing = false
                        loading = false
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapter.setNotes(notes)
                    }
                })
    }

    fun loadMoreNotes() {
        adapter.setLoading(true)
        App.get().gitLab.getIssueNotes(nextPageUrl!!.toString())
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Note>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        adapter.setLoading(false)
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun responseNonNullSuccess(notes: List<Note>) {
                        adapter.setLoading(false)
                        loading = false
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapter.addNotes(notes)
                    }
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
