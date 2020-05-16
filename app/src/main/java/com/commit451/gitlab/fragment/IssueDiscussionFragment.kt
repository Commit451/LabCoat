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
import com.commit451.alakazam.fadeIn
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.AttachActivity
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.api.response.FileUploadResponse
import com.commit451.gitlab.event.IssueChangedEvent
import com.commit451.gitlab.extension.mapResponseSuccessResponse
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Note
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.TransitionFactory
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.view.SendMessageView
import com.commit451.gitlab.viewHolder.NoteViewHolder
import com.commit451.teleprinter.Teleprinter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_issue_discussion.*
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

    private lateinit var adapter: BaseAdapter<Note, NoteViewHolder>
    private lateinit var loadHelper: LoadHelper<Note>
    private lateinit var teleprinter: Teleprinter

    private lateinit var project: Project
    private lateinit var issue: Issue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments?.getParcelable(KEY_PROJECT)!!
        issue = arguments?.getParcelable(KEY_ISSUE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_issue_discussion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        teleprinter = Teleprinter(baseActivty)

        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    NoteViewHolder.inflate(parent)
                },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item, project) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listNotes,
                baseAdapter = adapter,
                layoutManager = layoutManager,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = { gitLab.getIssueNotes(project.id, issue.iid) },
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

    private fun postNote(message: String) {
        if (message.isBlank()) {
            return
        }

        fullscreenProgress.fadeIn()
        // Clear text & collapse keyboard
        teleprinter.hideKeyboard()
        sendMessageView.clearText()

        gitLab.addIssueNote(project.id, issue.iid, message)
                .mapResponseSuccessResponse()
                .with(this)
                .subscribe({
                    if (it.first.code() == 202) {
                        load()
                    } else {
                        fullscreenProgress.isVisible = false
                        textMessage.isVisible = false
                        adapter.add(it.second, 0)
                        listNotes.smoothScrollToPosition(0)
                    }
                }, {
                    Timber.e(it)
                    fullscreenProgress.isVisible = false
                    Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                            .show()
                })
    }

    @Subscribe
    fun onEvent(event: IssueChangedEvent) {
        if (issue.iid == event.issue.iid) {
            issue = event.issue
            load()
        }
    }
}
