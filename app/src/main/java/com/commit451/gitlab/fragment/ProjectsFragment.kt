package com.commit451.gitlab.fragment

import android.content.Context
import android.net.Uri
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
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.adapter.ProjectAdapter
import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.parceler.Parcels
import retrofit2.Response
import timber.log.Timber

class ProjectsFragment : ButterKnifeFragment() {

    companion object {

        private val EXTRA_MODE = "extra_mode"
        private val EXTRA_QUERY = "extra_query"
        private val EXTRA_GROUP = "extra_group"

        val MODE_ALL = 0
        val MODE_MINE = 1
        val MODE_STARRED = 2
        val MODE_SEARCH = 3
        val MODE_GROUP = 4

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
            args.putParcelable(EXTRA_GROUP, Parcels.wrap(group))
            val fragment = ProjectsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.swipe_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list)
    lateinit var listProjects: RecyclerView
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView

    lateinit var layoutManagerProjects: LinearLayoutManager
    lateinit var adapterProjects: ProjectAdapter

    var mode: Int = 0
    var query: String? = null
    var nextPageUrl: Uri? = null
    var loading = false
    var listener: Listener? = null

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerProjects.childCount
            val totalItemCount = layoutManagerProjects.itemCount
            val firstVisibleItem = layoutManagerProjects.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context as Listener?
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = arguments.getInt(EXTRA_MODE)
        query = arguments.getString(EXTRA_QUERY)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_projects, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterProjects = ProjectAdapter(activity, object : ProjectAdapter.Listener {
            override fun onProjectClicked(project: Project) {
                if (listener == null) {
                    Navigator.navigateToProject(activity, project)
                } else {
                    listener!!.onProjectClicked(project)
                }
            }
        })
        layoutManagerProjects = LinearLayoutManager(activity)
        listProjects.layoutManager = layoutManagerProjects
        listProjects.addItemDecoration(DividerItemDecoration(activity))
        listProjects.adapter = adapterProjects
        listProjects.addOnScrollListener(onScrollListener)

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        loadData()
    }

    override fun loadData() {
        if (view == null) {
            return
        }
        textMessage.visibility = View.GONE

        nextPageUrl = null

        when (mode) {
            MODE_ALL -> {
                showLoading()
                actuallyLoadIt(getGitLab().getAllProjects())
            }
            MODE_MINE -> {
                showLoading()
                actuallyLoadIt(getGitLab().getMyProjects())
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
                val group = Parcels.unwrap<Group>(arguments.getParcelable<Parcelable>(EXTRA_GROUP)) ?: throw IllegalStateException("You must also pass a group if you want to show a groups projects")
                actuallyLoadIt(getGitLab().getGroupProjects(group.id))
            }
            else -> throw IllegalStateException(mode.toString() + " is not defined")
        }
    }

    private fun actuallyLoadIt(observable: Single<Response<List<Project>>>) {
        observable.compose(this.bindToLifecycle<Response<List<Project>>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<List<Project>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error)
                        adapterProjects.setData(null)
                        nextPageUrl = null
                    }

                    override fun responseSuccess(projects: List<Project>) {
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        if (projects.isEmpty()) {
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_projects)
                        }
                        adapterProjects.setData(projects)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url " + nextPageUrl)
                    }
                })
    }

    private fun loadMore() {
        if (view == null) {
            return
        }

        if (nextPageUrl == null) {
            return
        }
        loading = true
        adapterProjects.setLoading(true)
        Timber.d("loadMore called for %s", nextPageUrl)
        getGitLab().getProjects(nextPageUrl!!.toString())
                .compose(this.bindToLifecycle<Response<List<Project>>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<List<Project>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        adapterProjects.setLoading(false)
                    }

                    override fun responseSuccess(projects: List<Project>) {
                        loading = false
                        adapterProjects.setLoading(false)
                        adapterProjects.addData(projects)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url " + nextPageUrl)
                    }
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

    fun getGitLab(): GitLab {
        if (listener != null) {
            return listener!!.getGitLab()
        } else {
            return App.get().gitLab
        }
    }

    interface Listener {
        fun onProjectClicked(project: Project)

        fun getGitLab(): GitLab
    }
}
