package com.commit451.gitlab.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.adapter.ProjectAdapter
import com.commit451.gitlab.api.GitLabService
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_projects.*
import retrofit2.Response
import timber.log.Timber

class ProjectsFragment : BaseFragment() {

    companion object {

        private const val EXTRA_MODE = "extra_mode"
        private const val EXTRA_QUERY = "extra_query"
        private const val EXTRA_GROUP = "extra_group"

        const val MODE_ALL = 0
        const val MODE_MINE = 1
        const val MODE_STARRED = 2
        const val MODE_SEARCH = 3
        const val MODE_GROUP = 4

        fun newInstance(mode: Int): ProjectsFragment {
            val args = Bundle()
            args.putInt(EXTRA_MODE, mode)

            val fragment = ProjectsFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(searchTerm: String): ProjectsFragment {
            val args = Bundle()
            args.putInt(EXTRA_MODE, MODE_SEARCH)
            args.putString(EXTRA_QUERY, searchTerm)
            val fragment = ProjectsFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(group: Group): ProjectsFragment {
            val args = Bundle()
            args.putInt(EXTRA_MODE, MODE_GROUP)
            args.putParcelable(EXTRA_GROUP, group)
            val fragment = ProjectsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var layoutManagerProjects: LinearLayoutManager
    private lateinit var adapterProjects: ProjectAdapter

    private var mode: Int = 0
    private var query: String? = null
    private var nextPageUrl: String? = null
    private var loading = false
    private var listener: Listener? = null

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerProjects.childCount
            val totalItemCount = layoutManagerProjects.itemCount
            val firstVisibleItem = layoutManagerProjects.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = arguments?.getInt(EXTRA_MODE)!!
        query = arguments?.getString(EXTRA_QUERY)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_projects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterProjects = ProjectAdapter(baseActivty, object : ProjectAdapter.Listener {
            override fun onProjectClicked(project: Project) {
                if (listener == null) {
                    Navigator.navigateToProject(baseActivty, project)
                } else {
                    listener!!.onProjectClicked(project)
                }
            }
        })
        layoutManagerProjects = LinearLayoutManager(activity)
        listProjects.layoutManager = layoutManagerProjects
        listProjects.addItemDecoration(DividerItemDecoration(baseActivty))
        listProjects.adapter = adapterProjects
        listProjects.addOnScrollListener(onScrollListener)

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        loadData()
    }

    override fun loadData() {
        textMessage.visibility = View.GONE

        nextPageUrl = null

        when (mode) {
            MODE_ALL -> {
                showLoading()
                actuallyLoadIt(getGitLab().getAllProjects())
            }
            MODE_MINE -> {
                showLoading()
                actuallyLoadIt(getGitLab().getMyProjects(baseActivty.account.username!!))
            }
            MODE_STARRED -> {
                showLoading()
                actuallyLoadIt(getGitLab().getStarredProjects())
            }
            MODE_SEARCH -> if (query != null) {
                showLoading()
                actuallyLoadIt(getGitLab().searchAllProjects(query!!))
            }
            MODE_GROUP -> {
                showLoading()
                val group = arguments?.getParcelable<Group>(EXTRA_GROUP)
                        ?: throw IllegalStateException("You must also pass a group if you want to show a groups projects")
                actuallyLoadIt(getGitLab().getGroupProjects(group.id))
            }
            else -> throw IllegalStateException("$mode is not defined")
        }
    }

    private fun actuallyLoadIt(observable: Single<Response<List<Project>>>) {
        observable
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    swipeRefreshLayout.isRefreshing = false
                    if (it.body.isEmpty()) {
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.no_projects)
                    }
                    adapterProjects.setData(it.body)
                    nextPageUrl = it.paginationData.next
                    Timber.d("Next page url $nextPageUrl")
                }, {
                    loading = false
                    Timber.e(it)
                    swipeRefreshLayout.isRefreshing = false
                    textMessage.visibility = View.VISIBLE
                    textMessage.setText(R.string.connection_error)
                    adapterProjects.setData(null)
                    nextPageUrl = null
                })
    }

    private fun loadMore() {
        if (nextPageUrl == null) {
            return
        }
        loading = true
        adapterProjects.setLoading(true)
        Timber.d("loadMore called for %s", nextPageUrl)
        getGitLab().getProjects(nextPageUrl!!.toString())
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    adapterProjects.setLoading(false)
                    adapterProjects.addData(it.body)
                    nextPageUrl = it.paginationData.next
                    Timber.d("Next page url $nextPageUrl")
                }, {
                    loading = false
                    Timber.e(it)
                    adapterProjects.setLoading(false)
                })
    }

    private fun showLoading() {
        loading = true
        swipeRefreshLayout.isRefreshing = true
    }

    fun searchQuery(query: String) {
        this.query = query
        adapterProjects.clearData()
        loadData()
    }

    private fun getGitLab(): GitLabService {
        return listener?.getGitLab() ?: App.get().gitLab
    }

    interface Listener {
        fun onProjectClicked(project: Project)

        fun getGitLab(): GitLabService
    }
}
