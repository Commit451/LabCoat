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
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.adapter.MergeRequestAdapter
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Milestone
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.CommitViewHolder
import com.commit451.gitlab.viewHolder.MergeRequestViewHolder
import com.commit451.gitlab.viewHolder.MilestoneViewHolder
import kotlinx.android.synthetic.main.fragment_merge_request.*
import kotlinx.android.synthetic.main.fragment_merge_request.swipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_merge_request.textMessage
import kotlinx.android.synthetic.main.fragment_milestones.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

class MergeRequestsFragment : BaseFragment() {

    companion object {

        fun newInstance(): MergeRequestsFragment {
            return MergeRequestsFragment()
        }
    }

    private lateinit var adapter: BaseAdapter<MergeRequest, MergeRequestViewHolder>
    private lateinit var loadHelper: LoadHelper<MergeRequest>

    private lateinit var state: String
    private lateinit var states: Array<String>
    private var project: Project? = null

    private val onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            state = states[position]
            loadData()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        states = baseActivty.resources.getStringArray(R.array.merge_request_state_values)
        state = states.first()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_merge_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = MergeRequestViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val mergeRequest = adapter.items[viewHolder.adapterPosition]
                        Navigator.navigateToMergeRequest(baseActivty, project!!, mergeRequest)
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listMergeRequests,
                baseAdapter = adapter,
                swipeRefreshLayout = swipeRefreshLayout,
                dividers = true,
                errorOrEmptyTextView = textMessage,
                loadInitial = { gitLab.getMergeRequests(project!!.id, state) },
                loadMore = { gitLab.loadAnyList(it) }
        )

        spinnerState.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, resources.getStringArray(R.array.merge_request_state_names))
        spinnerState.onItemSelectedListener = onItemSelectedListener

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

    @Suppress("unused")
    @Subscribe
    fun onProjectReload(event: ProjectReloadEvent) {
        project = event.project
        loadData()
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @Subscribe
    fun onMergeRequestChanged(event: MergeRequestChangedEvent) {
        loadData()
    }
}
