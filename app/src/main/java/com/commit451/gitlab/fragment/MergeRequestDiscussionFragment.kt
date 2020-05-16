package com.commit451.gitlab.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.addendum.design.snackbar
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.AttachActivity
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.api.response.FileUploadResponse
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Note
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.TransitionFactory
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.view.SendMessageView
import com.commit451.gitlab.viewHolder.NoteViewHolder
import com.commit451.teleprinter.Teleprinter
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

    private lateinit var adapter: BaseAdapter<Note, NoteViewHolder>
    private lateinit var loadHelper: LoadHelper<Note>
    private lateinit var teleprinter: Teleprinter

    private lateinit var project: Project
    private lateinit var mergeRequest: MergeRequest

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

        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, true)
        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ -> NoteViewHolder.inflate(parent) },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item, project) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listNotes,
                baseAdapter = adapter,
                layoutManager = layoutManager,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = { gitLab.getMergeRequestNotes(project.id, mergeRequest.iid) },
                loadMore = { gitLab.loadAnyList(it) }
        )

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
        load()

        App.bus().register(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ATTACH -> {
                if (resultCode == RESULT_OK) {
                    val response = data?.getParcelableExtra<FileUploadResponse>(AttachActivity.KEY_FILE_UPLOAD_RESPONSE)!!
                    fullscreenProgress.visibility = View.GONE
                    sendMessageView.appendText(response.markdown)
                } else {
                    root.snackbar(R.string.failed_to_upload_file)
                }
            }
        }
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    private fun load() {
        loadHelper.load()
    }

    fun postNote(message: String) {

        if (message.isBlank()) {
            return
        }

        fullscreenProgress.visibility = View.VISIBLE
        fullscreenProgress.alpha = 0.0f
        fullscreenProgress.animate().alpha(1.0f)
        // Clear text & collapse keyboard
        teleprinter.hideKeyboard()
        sendMessageView.clearText()

        App.get().gitLab.addMergeRequestNote(project.id, mergeRequest.iid, message)
                .with(this)
                .subscribe({
                    fullscreenProgress.visibility = View.GONE
                    textMessage.isVisible = false
                    adapter.add(it, 0)
                    listNotes.smoothScrollToPosition(0)
                }, {
                    Timber.e(it)
                    fullscreenProgress.visibility = View.GONE
                    root.snackbar(getString(R.string.connection_error))
                })
    }

    @Suppress("unused")
    @Subscribe
    fun onMergeRequestChangedEvent(event: MergeRequestChangedEvent) {
        if (mergeRequest.id == event.mergeRequest.id) {
            mergeRequest = event.mergeRequest
            load()
        }
    }
}
