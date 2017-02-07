package com.commit451.gitlab.fragment

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import butterknife.BindView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.adapter.BuildAdapter
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.event.BuildChangedEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Shows the builds of a project
 */
class BuildsFragment : ButterKnifeFragment() {

    companion object {

        fun newInstance(): BuildsFragment {
            return BuildsFragment()
        }
    }

    @BindView(R.id.root) lateinit var root: ViewGroup
    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list) lateinit var listBuilds: RecyclerView
    @BindView(R.id.message_text) lateinit var textMessage: TextView
    @BindView(R.id.issue_spinner) lateinit var spinnerIssue: Spinner

    lateinit var adapterBuilds: BuildAdapter
    lateinit var layoutManagerBuilds: LinearLayoutManager

    lateinit var scopes: Array<String>
    var scope: String? = null
    var project: Project? = null
    var nextPageUrl: Uri? = null
    var loading: Boolean = false

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerBuilds.childCount
            val totalItemCount = layoutManagerBuilds.itemCount
            val firstVisibleItem = layoutManagerBuilds.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scopes = resources.getStringArray(R.array.build_scope_values)
        scope = scopes[0]
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_builds, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapterBuilds = BuildAdapter(object : BuildAdapter.Listener {
            override fun onBuildClicked(build: Build) {
                if (project != null) {
                    Navigator.navigateToBuild(activity, project!!, build)
                } else {
                    Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
        })
        layoutManagerBuilds = LinearLayoutManager(activity)
        listBuilds.layoutManager = layoutManagerBuilds
        listBuilds.addItemDecoration(DividerItemDecoration(activity))
        listBuilds.adapter = adapterBuilds
        listBuilds.addOnScrollListener(onScrollListener)

        spinnerIssue.adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1,
                android.R.id.text1, resources.getStringArray(R.array.build_scope_names))
        spinnerIssue.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                scope = scopes[position]
                loadData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        if (activity is ProjectActivity) {
            project = (activity as ProjectActivity).project
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
        textMessage.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true
        nextPageUrl = null
        loading = true
        App.get().gitLab.getBuilds(project!!.id, scope)
                .setup(bindToLifecycle())
                .subscribe(object : CustomResponseSingleObserver<List<Build>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.failed_to_load_builds)
                        adapterBuilds.setValues(null)
                        nextPageUrl = null
                    }

                    override fun responseSuccess(builds: List<Build>) {
                        loading = false

                        swipeRefreshLayout.isRefreshing = false
                        if (builds.isEmpty()) {
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_builds)
                        }
                        adapterBuilds.setValues(builds)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url %s", nextPageUrl)
                    }
                })
    }

    fun loadMore() {
        if (nextPageUrl == null) {
            return
        }

        adapterBuilds.setLoading(true)
        loading = true

        Timber.d("loadMore called for %s", nextPageUrl)
        App.get().gitLab.getBuilds(nextPageUrl!!.toString(), scope)
                .setup(bindToLifecycle())
                .subscribe(object : CustomResponseSingleObserver<List<Build>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        loading = false
                        adapterBuilds.setLoading(false)
                    }

                    override fun responseSuccess(builds: List<Build>) {
                        loading = false
                        adapterBuilds.setLoading(false)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapterBuilds.addValues(builds)
                    }
                })
    }

    @Subscribe
    fun onEvent(event: ProjectReloadEvent) {
        project = event.project
        loadData()
    }

    @Subscribe
    fun onEvent(event: BuildChangedEvent) {
        adapterBuilds.updateBuild(event.build)
    }
}