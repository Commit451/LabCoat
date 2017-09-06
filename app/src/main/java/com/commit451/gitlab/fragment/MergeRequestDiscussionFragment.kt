package com.commit451.gitlab.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import com.commit451.addendum.parceler.getParcelerParcelable
import com.commit451.addendum.parceler.getParcelerParcelableExtra
import com.commit451.addendum.parceler.putParcelerParcelable
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.AttachActivity
import com.commit451.gitlab.adapter.MergeRequestDetailAdapter
import com.commit451.gitlab.api.response.FileUploadResponse
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Note
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.TransitionFactory
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import com.commit451.gitlab.view.SendMessageView
import com.commit451.teleprinter.Teleprinter
import com.trello.rxlifecycle2.android.FragmentEvent
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Shows the discussion of a merge request
 */
class MergeRequestDiscussionFragment : ButterKnifeFragment() {

    companion object {

        private val KEY_PROJECT = "project"
        private val KEY_MERGE_REQUEST = "merge_request"

        private val REQUEST_ATTACH = 1

        fun newInstance(project: Project, mergeRequest: MergeRequest): MergeRequestDiscussionFragment {
            val fragment = MergeRequestDiscussionFragment()
            val args = Bundle()
            args.putParcelerParcelable(KEY_PROJECT, project)
            args.putParcelerParcelable(KEY_MERGE_REQUEST, mergeRequest)
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.root) lateinit var root: ViewGroup
    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list) lateinit var listNotes: RecyclerView
    @BindView(R.id.send_message_view) lateinit var sendMessageView: SendMessageView
    @BindView(R.id.progress) lateinit var progress: View

    lateinit var adapterMergeRequestDetail: MergeRequestDetailAdapter
    lateinit var layoutManagerNotes: LinearLayoutManager
    lateinit var teleprinter: Teleprinter

    lateinit var project: Project
    lateinit var mergeRequest: MergeRequest
    var nextPageUrl: Uri? = null
    var loading: Boolean = false

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments.getParcelerParcelable<Project>(KEY_PROJECT)!!
        mergeRequest = arguments.getParcelerParcelable<MergeRequest>(KEY_MERGE_REQUEST)!!
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_merge_request_discussion, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        teleprinter = Teleprinter(activity)

        adapterMergeRequestDetail = MergeRequestDetailAdapter(activity, mergeRequest, project)
        layoutManagerNotes = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true)
        listNotes.layoutManager = layoutManagerNotes
        listNotes.adapter = adapterMergeRequestDetail
        listNotes.addOnScrollListener(onScrollListener)

        sendMessageView.callback = object : SendMessageView.Callback {
            override fun onSendClicked(message: String) {
                postNote(message)
            }

            override fun onAttachmentClicked() {
                val intent = AttachActivity.newIntent(activity, project)
                val activityOptions = TransitionFactory.createFadeInOptions(activity)
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
            REQUEST_ATTACH ->  {
                if (resultCode == RESULT_OK) {
                    val response = data!!.getParcelerParcelableExtra<FileUploadResponse>(AttachActivity.KEY_FILE_UPLOAD_RESPONSE)!!
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

    fun loadNotes() {
        swipeRefreshLayout.isRefreshing = true
        App.get().gitLab.getMergeRequestNotes(project.id, mergeRequest.iid)
                .setup(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
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
                        adapterMergeRequestDetail.setNotes(notes)
                    }
                })
    }

    fun loadMoreNotes() {
        adapterMergeRequestDetail.setLoading(true)
        App.get().gitLab.getMergeRequestNotes(nextPageUrl!!.toString())
                .setup(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(object : CustomResponseSingleObserver<List<Note>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        adapterMergeRequestDetail.setLoading(false)
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun responseNonNullSuccess(notes: List<Note>) {
                        adapterMergeRequestDetail.setLoading(false)
                        loading = false
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapterMergeRequestDetail.addNotes(notes)
                    }
                })
    }

    fun postNote(message: String) {

        if (message.isNullOrBlank()) {
            return
        }

        progress.visibility = View.VISIBLE
        progress.alpha = 0.0f
        progress.animate().alpha(1.0f)
        // Clear text & collapse keyboard
        teleprinter.hideKeyboard()
        sendMessageView.clearText()

        App.get().gitLab.addMergeRequestNote(project.id, mergeRequest.id, message)
                .setup(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(object : CustomSingleObserver<Note>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        progress.visibility = View.GONE
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun success(note: Note) {
                        progress.visibility = View.GONE
                        adapterMergeRequestDetail.addNote(note)
                        listNotes.smoothScrollToPosition(MergeRequestDetailAdapter.headerCount)
                    }
                })
    }

    @Subscribe
    fun onMergeRequestChangedEvent(event: MergeRequestChangedEvent) {
        if (mergeRequest.id == event.mergeRequest.id) {
            mergeRequest = event.mergeRequest
            loadNotes()
        }
    }
}
