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
import com.commit451.gitlab.event.IssueChangedEvent
import com.commit451.gitlab.event.IssueCreatedEvent
import com.commit451.gitlab.event.IssueReloadEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.IssueViewHolder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_issues.*
import org.greenrobot.eventbus.Subscribe

class IssuesFragment : BaseFragment() {

    companion object {

        fun newInstance(): IssuesFragment {
            return IssuesFragment()
        }
    }

    private var project: Project? = null
    private lateinit var state: String
    private lateinit var states: Array<String>

    private lateinit var adapter: BaseAdapter<Issue, IssueViewHolder>
    private lateinit var loadHelper: LoadHelper<Issue>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = resources.getString(R.string.issue_state_value_default)
        states = resources.getStringArray(R.array.issue_state_values)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_issues, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        buttonAddIssue.setOnClickListener {
            if (project != null) {
                Navigator.navigateToAddIssue(baseActivty, buttonAddIssue, project!!)
            } else {
                Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                        .show()
            }
        }
        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = IssueViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val issue = adapter.items[viewHolder.adapterPosition]
                        Navigator.navigateToIssue(baseActivty, project!!, issue)
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listIssues,
                baseAdapter = adapter,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = {
                    gitLab.getIssues(project!!.id, state)
                },
                loadMore = {
                    gitLab.loadAnyList(it)
                }
        )
        listIssues.addItemDecoration(DividerItemDecoration(baseActivty))

        spinnerIssue.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, resources.getStringArray(R.array.issue_state_names))
        spinnerIssue.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                state = states[position]
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
    fun onEvent(event: IssueCreatedEvent) {
        adapter.add(event.issue, 0)
        if (view != null) {
            textMessage.visibility = View.GONE
            listIssues.smoothScrollToPosition(0)
        }
    }

    @Subscribe
    fun onEvent(event: IssueChangedEvent) {
        adapter.update(event.issue)
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onEvent(event: IssueReloadEvent) {
        loadData()
    }
}
