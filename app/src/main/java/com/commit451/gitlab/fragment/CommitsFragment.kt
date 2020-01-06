package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.android.synthetic.main.fragment_commits.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

class CommitsFragment : BaseFragment() {

    companion object {

        fun newInstance(): CommitsFragment {
            return CommitsFragment()
        }
    }

    private lateinit var layoutManagerCommits: LinearLayoutManager
    private lateinit var adapterCommits: CommitAdapter

    private var project: Project? = null
    private var branchName: String? = null
    private var page = -1
    private var loading: Boolean = false

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
                .subscribe({ repositoryCommits ->
                    loading = false
                    swipeRefreshLayout.isRefreshing = false
                    if (repositoryCommits.isNotEmpty()) {
                        textMessage.visibility = View.GONE
                    } else {
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.no_commits_found)
                    }
                    adapterCommits.setData(repositoryCommits)
                    if (repositoryCommits.isEmpty()) {
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
                .subscribe({
                    loading = false
                    adapterCommits.setLoading(false)
                    if (it.isEmpty()) {
                        page = -1
                    } else {
                        adapterCommits.addData(it)
                    }
                }, {
                    loading = false
                    Timber.e(it)
                    adapterCommits.setLoading(false)
                })
    }

    @Suppress("unused")
    @Subscribe
    fun onProjectReload(event: ProjectReloadEvent) {
        project = event.project
        branchName = event.branchName
        loadData()
    }
}
