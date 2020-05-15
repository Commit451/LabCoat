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
import com.commit451.gitlab.event.BuildChangedEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.BuildViewHolder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_jobs.*
import org.greenrobot.eventbus.Subscribe

/**
 * Shows the jobs of a project
 */
class JobsFragment : BaseFragment() {

    companion object {

        fun newInstance(): JobsFragment {
            return JobsFragment()
        }
    }

    private lateinit var adapter: BaseAdapter<Build, BuildViewHolder>
    private lateinit var loadHelper: LoadHelper<Build>

    private lateinit var scopes: Array<String>
    private var scope: String? = null
    private var project: Project? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scopes = resources.getStringArray(R.array.build_scope_values)
        scope = scopes.first()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = BuildViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val build = adapter.items[viewHolder.adapterPosition]
                        if (project != null) {
                            Navigator.navigateToBuild(baseActivty, project!!, build)
                        } else {
                            Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                                    .show()
                        }
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listBuilds,
                baseAdapter = adapter,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = {
                    gitLab.getBuilds(project!!.id, scope)
                },
                loadMore = {
                    gitLab.loadAnyList(it)
                }
        )

        spinnerIssue.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1,
                android.R.id.text1, resources.getStringArray(R.array.build_scope_names))
        spinnerIssue.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                scope = scopes[position]
                loadData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
        loadHelper.load()
    }

    @Subscribe
    fun onEvent(event: ProjectReloadEvent) {
        project = event.project
        loadData()
    }

    @Subscribe
    fun onEvent(event: BuildChangedEvent) {
        adapter.update(event.build)
    }
}
