package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.event.PipelineChangedEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.model.api.Pipeline
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.CommitViewHolder
import com.commit451.gitlab.viewHolder.PipelineViewHolder
import kotlinx.android.synthetic.main.fragment_pipelines.*
import org.greenrobot.eventbus.Subscribe

/**
 * Shows the pipelines of a project
 */
class PipelinesFragment : BaseFragment() {

    companion object {

        fun newInstance(): PipelinesFragment {
            return PipelinesFragment()
        }
    }

    private lateinit var adapter: BaseAdapter<Pipeline, PipelineViewHolder>
    private lateinit var loadHelper: LoadHelper<Pipeline>

    private lateinit var scopes: Array<String>
    private var scope: String? = null
    private var project: Project? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scopes = resources.getStringArray(R.array.pipeline_scope_values)
        scope = scopes.first()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pipelines, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = CommitViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val pipeline = adapter.items[viewHolder.adapterPosition]
                        Navigator.navigateToPipeline(baseActivty, project!!, pipeline)
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listPipelines,
                baseAdapter = adapter,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = { gitLab.getPipelines(project!!.id, scope) },
                loadMore = { gitLab.loadAnyList(it) }
        )
        listPipelines.addItemDecoration(DividerItemDecoration(baseActivty))

        spinnerIssue.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1,
                android.R.id.text1, resources.getStringArray(R.array.pipeline_scope_names))
        spinnerIssue.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                scope = scopes[position]
                loadData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

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
        loadHelper.load()
    }

    @Subscribe
    fun onEvent(event: ProjectReloadEvent) {
        project = event.project
        loadData()
    }

    @Subscribe
    fun onEvent(event: PipelineChangedEvent) {
        adapter.update(event.pipeline)
    }
}
