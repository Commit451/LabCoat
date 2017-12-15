package com.commit451.gitlab.fragment

import android.os.Bundle
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
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.adapter.CommitAdapter
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryCommit
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomSingleObserver
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

class CommitsFragment : ButterKnifeFragment() {

    companion object {

        fun newInstance(): CommitsFragment {
            return CommitsFragment()
        }
    }

    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list) lateinit var listCommits: RecyclerView
    @BindView(R.id.message_text) lateinit var textMessage: TextView

    lateinit var layoutManagerCommits: LinearLayoutManager
    lateinit var adapterCommits: CommitAdapter

    var project: Project? = null
    var branchName: String? = null
    internal var page = -1
    internal var loading: Boolean = false

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_commits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

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

        if (activity is ProjectActivity) {
            project = (activity as ProjectActivity).project
            branchName = (activity as ProjectActivity).getRefRef()
            loadData()
        } else {
            throw IllegalStateException("Incorrect parent activity")
        }
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    override fun loadData() {
        if (view == null) {
            return
        }

        if (project == null || branchName.isNullOrEmpty()) {
            swipeRefreshLayout.isRefreshing = false
            return
        }

        swipeRefreshLayout.isRefreshing = true

        page = 1
        loading = true

        App.get().gitLab.getCommits(project!!.id, branchName!!, page)
                .with(this)
                .subscribe(object : CustomSingleObserver<List<RepositoryCommit>>() {

                    override fun error(t: Throwable) {
                        loading = false
                        Timber.e(t)
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

        if (project == null || branchName.isNullOrEmpty() || page < 0) {
            return
        }

        page++
        loading = true
        adapterCommits.setLoading(true)

        Timber.d("loadMore called for %s", page)
        App.get().gitLab.getCommits(project!!.id, branchName!!, page)
                .with(this)
                .subscribe(object : CustomSingleObserver<List<RepositoryCommit>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        adapterCommits.setLoading(false)
                    }

                    override fun success(repositoryCommits: List<RepositoryCommit>) {
                        loading = false
                        adapterCommits.setLoading(false)
                        if (repositoryCommits.isEmpty()) {
                            page = -1
                            return
                        }
                        adapterCommits.addData(repositoryCommits)
                    }
                })
    }

    @Subscribe
    fun onProjectReload(event: ProjectReloadEvent) {
        project = event.project
        branchName = event.branchName
        loadData()
    }
}