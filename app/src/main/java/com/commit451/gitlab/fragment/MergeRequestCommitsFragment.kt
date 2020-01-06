package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.CommitAdapter
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryCommit
import com.commit451.gitlab.navigation.Navigator
import kotlinx.android.synthetic.main.fragment_merge_request_commits.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Like [CommitsFragment] but showing commits for a merge request
 */
class MergeRequestCommitsFragment : BaseFragment() {

    companion object {

        private const val KEY_PROJECT = "project"
        private const val KEY_MERGE_REQUEST = "merge_request"

        fun newInstance(project: Project, mergeRequest: MergeRequest): MergeRequestCommitsFragment {
            val fragment = MergeRequestCommitsFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, project)
            args.putParcelable(KEY_MERGE_REQUEST, mergeRequest)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var layoutManagerCommits: LinearLayoutManager
    private lateinit var adapterCommits: CommitAdapter

    private var project: Project? = null
    private var mergeRequest: MergeRequest? = null
    private var page = -1
    private var loading = false

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerCommits.childCount
            val totalItemCount = layoutManagerCommits.itemCount
            val firstVisibleItem = layoutManagerCommits.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && page >= 0) {
                loadMore()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments?.getParcelable(KEY_PROJECT)
        mergeRequest = arguments?.getParcelable(KEY_MERGE_REQUEST)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_merge_request_commits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterCommits = CommitAdapter(object : CommitAdapter.Listener {
            override fun onCommitClicked(commit: RepositoryCommit) {
                Navigator.navigateToDiffActivity(baseActivty, project!!, commit)
            }
        })
        layoutManagerCommits = LinearLayoutManager(activity)
        listCommits.layoutManager = layoutManagerCommits
        listCommits.addItemDecoration(DividerItemDecoration(baseActivty))
        listCommits.adapter = adapterCommits
        listCommits.addOnScrollListener(onScrollListener)

        swipeRefreshLayout.setOnRefreshListener { loadData() }
        loadData()
        App.bus().register(this)
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    override fun loadData() {
        if (view == null) {
            return
        }

        swipeRefreshLayout.isRefreshing = true

        page = 0
        loading = true

        App.get().gitLab.getMergeRequestCommits(project!!.id, mergeRequest!!.iid)
                .with(this)
                .subscribe({
                    loading = false
                    swipeRefreshLayout.isRefreshing = false
                    if (it.isNotEmpty()) {
                        textMessage.visibility = View.GONE
                    } else {
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.no_commits_found)
                    }
                    adapterCommits.setData(it)
                    if (it.isEmpty()) {
                        page = -1
                    }
                }, {
                    loading = false
                    Timber.e(it)
                    swipeRefreshLayout.isRefreshing = false
                    textMessage.visibility = View.VISIBLE
                    textMessage.setText(R.string.connection_error_commits)
                    adapterCommits.setData(null)
                    page = -1
                })
    }

    fun loadMore() {
        page++
        loading = true
        //adapterCommits.setLoading(true);

        Timber.d("loadMore called for %s", page)
        //TODO is this even a thing?
    }

    @Suppress("unused")
    @Subscribe
    fun onMergeRequestChangedEvent(event: MergeRequestChangedEvent) {
        if (mergeRequest!!.iid == event.mergeRequest.id) {
            mergeRequest = event.mergeRequest
            loadData()
        }
    }
}
