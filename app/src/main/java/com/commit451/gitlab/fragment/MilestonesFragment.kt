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
import com.commit451.gitlab.event.MilestoneChangedEvent
import com.commit451.gitlab.event.MilestoneCreatedEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.model.api.Milestone
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.CommitViewHolder
import com.commit451.gitlab.viewHolder.MilestoneViewHolder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_commits.*
import kotlinx.android.synthetic.main.fragment_milestones.*
import kotlinx.android.synthetic.main.fragment_milestones.swipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_milestones.textMessage
import org.greenrobot.eventbus.Subscribe

class MilestonesFragment : BaseFragment() {

    companion object {

        fun newInstance(): MilestonesFragment {
            return MilestonesFragment()
        }
    }

    private lateinit var adapter: BaseAdapter<Milestone, MilestoneViewHolder>
    private lateinit var loadHelper: LoadHelper<Milestone>

    private var state: String? = null
    private lateinit var states: Array<String>
    private var project: Project? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        states = resources.getStringArray(R.array.milestone_state_values)
        state = states.first()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_milestones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        buttonAdd.setOnClickListener {
            if (project != null) {
                Navigator.navigateToAddMilestone(baseActivty, buttonAdd, project!!)
            } else {
                Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                        .show()
            }
        }

        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = CommitViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val milestone = adapter.items[viewHolder.adapterPosition]
                        Navigator.navigateToMilestone(baseActivty, project!!, milestone)
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listMilestones,
                baseAdapter = adapter,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = { gitLab.getMilestones(project!!.id, state) },
                loadMore = { gitLab.loadAnyList(it) }
        )
        listMilestones.addItemDecoration(DividerItemDecoration(baseActivty))

        spinnerStates.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, resources.getStringArray(R.array.milestone_state_names))
        spinnerStates.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
    fun onEvent(event: MilestoneCreatedEvent) {
        adapter.add(event.milestone, 0)
        textMessage.visibility = View.GONE
        listMilestones.smoothScrollToPosition(0)
    }

    @Subscribe
    fun onEvent(event: MilestoneChangedEvent) {
        adapter.update(event.milestone)
    }
}
