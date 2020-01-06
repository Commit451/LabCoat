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
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.adapter.PipelineAdapter
import com.commit451.gitlab.event.PipelineChangedEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Pipeline
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_pipelines.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Shows the pipelines of a project
 */
class PipelinesFragment : BaseFragment() {

    companion object {

        fun newInstance(): PipelinesFragment {
            return PipelinesFragment()
        }
    }

    private lateinit var adapterPipelines: PipelineAdapter
    private lateinit var layoutManagerPipelines: LinearLayoutManager

    private lateinit var scopes: Array<String>
    private var scope: String? = null
    private var project: Project? = null
    private var nextPageUrl: String? = null
    private var loading: Boolean = false

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerPipelines.childCount
            val totalItemCount = layoutManagerPipelines.itemCount
            val firstVisibleItem = layoutManagerPipelines.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scopes = resources.getStringArray(R.array.pipeline_scope_values)
        scope = scopes[0]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pipelines, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapterPipelines = PipelineAdapter(object : PipelineAdapter.Listener {
            override fun onPipelinesClicked(pipeline: Pipeline) {
                if (project != null) {
                    Navigator.navigateToPipeline(baseActivty, project!!, pipeline)
                } else {
                    Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
        })
        layoutManagerPipelines = LinearLayoutManager(activity)
        listPipelines.layoutManager = layoutManagerPipelines
        listPipelines.addItemDecoration(DividerItemDecoration(baseActivty))
        listPipelines.adapter = adapterPipelines
        listPipelines.addOnScrollListener(onScrollListener)

        spinnerIssue.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1,
                android.R.id.text1, resources.getStringArray(R.array.pipeline_scope_names))
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
        App.get().gitLab.getPipelines(project!!.id, scope)
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    swipeRefreshLayout.isRefreshing = false
                    if (it.body.isEmpty()) {
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.no_pipelines)
                    }
                    adapterPipelines.setValues(it.body)
                    nextPageUrl = it.paginationData.next
                    Timber.d("Next page url %s", nextPageUrl)
                }, {
                    loading = false
                    Timber.e(it)
                    swipeRefreshLayout.isRefreshing = false
                    textMessage.visibility = View.VISIBLE
                    textMessage.setText(R.string.failed_to_load_pipelines)
                    adapterPipelines.setValues(null)
                    nextPageUrl = null
                })
    }

    fun loadMore() {
        if (nextPageUrl == null) {
            return
        }

        adapterPipelines.setLoading(true)
        loading = true

        Timber.d("loadMore called for %s", nextPageUrl)
        App.get().gitLab.getPipelines(nextPageUrl!!.toString(), scope)
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    adapterPipelines.setLoading(false)
                    nextPageUrl = it.paginationData.next
                    adapterPipelines.addValues(it.body)
                }, {
                    Timber.e(it)
                    loading = false
                    adapterPipelines.setLoading(false)
                })
    }

    @Subscribe
    fun onEvent(event: ProjectReloadEvent) {
        project = event.project
        loadData()
    }

    @Subscribe
    fun onEvent(event: PipelineChangedEvent) {
        adapterPipelines.updatePipeline(event.pipeline)
    }
}
