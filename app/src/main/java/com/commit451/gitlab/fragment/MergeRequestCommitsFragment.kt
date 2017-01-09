package com.commit451.gitlab.fragment

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.CommitAdapter
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryCommit
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomSingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.Subscribe
import org.parceler.Parcels
import timber.log.Timber

/**
 * Like [CommitsFragment] but showing commits for a merge request
 */
class MergeRequestCommitsFragment : ButterKnifeFragment() {

    companion object {

        private val KEY_PROJECT = "project"
        private val KEY_MERGE_REQUEST = "merge_request"

        fun newInstance(project: Project, mergeRequest: MergeRequest): MergeRequestCommitsFragment {
            val fragment = MergeRequestCommitsFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, Parcels.wrap(project))
            args.putParcelable(KEY_MERGE_REQUEST, Parcels.wrap(mergeRequest))
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.swipe_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list)
    lateinit var listCommits: RecyclerView
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView

    lateinit var layoutManagerCommits: LinearLayoutManager
    lateinit var adapterCommits: CommitAdapter

    var project: Project? = null
    var mergeRequest: MergeRequest? = null
    var page = -1
    var loading = false

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
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
        project = Parcels.unwrap<Project>(arguments.getParcelable<Parcelable>(KEY_PROJECT))
        mergeRequest = Parcels.unwrap<MergeRequest>(arguments.getParcelable<Parcelable>(KEY_MERGE_REQUEST))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_merge_request_commits, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterCommits = CommitAdapter(object : CommitAdapter.Listener {
            override fun onCommitClicked(commit: RepositoryCommit) {
                Navigator.navigateToDiffActivity(activity, project, commit)
            }
        })
        layoutManagerCommits = LinearLayoutManager(activity)
        listCommits.layoutManager = layoutManagerCommits
        listCommits.addItemDecoration(DividerItemDecoration(activity))
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

        App.get().gitLab.getMergeRequestCommits(project!!.id, mergeRequest!!.id)
                .compose(this.bindToLifecycle<List<RepositoryCommit>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomSingleObserver<List<RepositoryCommit>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error_commits)
                        adapterCommits.setData(null)
                        page = -1
                    }

                    override fun success(repositoryCommits: List<RepositoryCommit>) {
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        if (!repositoryCommits.isEmpty()) {
                            textMessage.visibility = View.GONE
                        } else {
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_commits_found)
                        }
                        adapterCommits.setData(repositoryCommits)
                        if (repositoryCommits.isEmpty()) {
                            page = -1
                        }
                    }
                })
    }

    fun loadMore() {
        if (view == null) {
            return
        }

        page++
        loading = true
        //adapterCommits.setLoading(true);

        Timber.d("loadMore called for %s", page)
        //TODO is this even a thing?
    }

    @Subscribe
    fun onMergeRequestChangedEvent(event: MergeRequestChangedEvent) {
        if (mergeRequest!!.id == event.mergeRequest.id) {
            mergeRequest = event.mergeRequest
            loadData()
        }
    }
}