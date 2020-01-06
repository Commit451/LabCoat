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
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Note
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.TransitionFactory
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import com.commit451.gitlab.view.SendMessageView
import com.commit451.teleprinter.Teleprinter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_merge_request_discussion.*
import kotlinx.android.synthetic.main.progress_fullscreen.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Shows the discussion of a merge request
 */
class MergeRequestDiscussionFragment : BaseFragment() {

    companion object {

        private const val KEY_PROJECT = "project"
        private const val KEY_MERGE_REQUEST = "merge_request"

        private const val REQUEST_ATTACH = 1

        fun newInstance(project: Project, mergeRequest: MergeRequest): MergeRequestDiscussionFragment {
            val fragment = MergeRequestDiscussionFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, project)
            args.putParcelable(KEY_MERGE_REQUEST, mergeRequest)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var adapterNotes: NotesAdapter
    private lateinit var layoutManagerNotes: LinearLayoutManager
    private lateinit var teleprinter: Teleprinter

    private lateinit var project: Project
    private lateinit var mergeRequest: MergeRequest
    private var nextPageUrl: Uri? = null
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
        mergeRequest = arguments?.getParcelable(KEY_MERGE_REQUEST)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_merge_request_discussion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        teleprinter = Teleprinter(baseActivty)

        adapterNotes = NotesAdapter(project)
        layoutManagerNotes = LinearLayoutManager(activity, RecyclerView.VERTICAL, true)
        listNotes.layoutManager = layoutManagerNotes
        listNotes.adapter = adapterNotes
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
        App.get().gitLab.getMergeRequestNotes(project.id, mergeRequest.iid)
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
                        adapterNotes.setNotes(notes)
                    }
                })
    }

    fun loadMoreNotes() {
        adapterNotes.setLoading(true)
        App.get().gitLab.getMergeRequestNotes(nextPageUrl!!.toString())
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Note>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        adapterNotes.setLoading(false)
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun responseNonNullSuccess(notes: List<Note>) {
                        adapterNotes.setLoading(false)
                        loading = false
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapterNotes.addNotes(notes)
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

        App.get().gitLab.addMergeRequestNote(project.id, mergeRequest.iid, message)
                .with(this)
                .subscribe({
                    progress.visibility = View.GONE
                    adapterNotes.addNote(it)
                    listNotes.smoothScrollToPosition(0)
                }, {
                    Timber.e(it)
                    progress.visibility = View.GONE
                    Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                            .show()
                })
    }

    @Suppress("unused")
    @Subscribe
    fun onMergeRequestChangedEvent(event: MergeRequestChangedEvent) {
        if (mergeRequest.id == event.mergeRequest.id) {
            mergeRequest = event.mergeRequest
            loadNotes()
        }
    }
}
