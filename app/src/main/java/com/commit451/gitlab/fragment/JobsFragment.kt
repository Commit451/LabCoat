package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.adapter.BuildAdapter
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.event.BuildChangedEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_jobs.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Shows the jobs of a project
 */
class JobsFragment : BaseFragment() {

    companion object {

        fun newInstance(): JobsFragment {
            return JobsFragment()
        }
    }

    private lateinit var adapterBuilds: BuildAdapter
    private lateinit var layoutManagerBuilds: LinearLayoutManager

    private lateinit var scopes: Array<String>
    private var scope: String? = null
    private var project: Project? = null
    private var nextPageUrl: String? = null
    private var loading: Boolean = false

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapterBuilds = BuildAdapter(object : BuildAdapter.Listener {
            override fun onBuildClicked(build: Build) {
                if (project != null) {
                    Navigator.navigateToBuild(baseActivty, project!!, build)
                } else {
                    Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
        })
        layoutManagerBuilds = LinearLayoutManager(activity)
        listBuilds.layoutManager = layoutManagerBuilds
        listBuilds.addItemDecoration(DividerItemDecoration(baseActivty))
        listBuilds.adapter = adapterBuilds
        listBuilds.addOnScrollListener(onScrollListener)

        spinnerIssue.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1,
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
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false

                    swipeRefreshLayout.isRefreshing = false
                    if (it.body.isEmpty()) {
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.no_builds)
                    }
                    adapterBuilds.setValues(it.body)
                    nextPageUrl = it.paginationData.next
                    Timber.d("Next page url %s", nextPageUrl)
                }, {
                    loading = false
                    Timber.e(it)
                    swipeRefreshLayout.isRefreshing = false
                    textMessage.visibility = View.VISIBLE
                    textMessage.setText(R.string.failed_to_load_builds)
                    adapterBuilds.setValues(null)
                    nextPageUrl = null
                })
    }

    fun loadMore() {
        if (nextPageUrl == null) {
            return
        }

        adapterBuilds.setLoading(true)
        loading = true

        Timber.d("loadMore called for %s", nextPageUrl)
        App.get().gitLab.getBuilds(nextPageUrl!!, scope)
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    adapterBuilds.setLoading(false)
                    nextPageUrl = it.paginationData.next
                    adapterBuilds.addValues(it.body)
                }, {
                    Timber.e(it)
                    loading = false
                    adapterBuilds.setLoading(false)
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
