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
import butterknife.OnClick
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.adapter.MilestoneAdapter
import com.commit451.gitlab.event.MilestoneChangedEvent
import com.commit451.gitlab.event.MilestoneCreatedEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Milestone
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

class MilestonesFragment : ButterKnifeFragment() {

    companion object {

        fun newInstance(): MilestonesFragment {
            return MilestonesFragment()
        }
    }

    @BindView(R.id.root) lateinit var root: ViewGroup
    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list) lateinit var listMilestones: RecyclerView
    @BindView(R.id.message_text) lateinit var textMessage: TextView
    @BindView(R.id.state_spinner) lateinit var spinnerStates: Spinner

    lateinit var adapterMilestones: MilestoneAdapter
    lateinit var layoutManagerMilestones: LinearLayoutManager

    var state: String? = null
    lateinit var states: Array<String>
    var project: Project? = null
    var loading = false
    var nextPageUrl: Uri? = null

    val mOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerMilestones.childCount
            val totalItemCount = layoutManagerMilestones.itemCount
            val firstVisibleItem = layoutManagerMilestones.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    @OnClick(R.id.add)
    fun onAddClicked(fab: View) {
        if (project != null) {
            Navigator.navigateToAddMilestone(baseActivty, fab, project!!)
        } else {
            Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                    .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = resources.getString(R.string.milestone_state_value_default)
        states = resources.getStringArray(R.array.milestone_state_values)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_milestones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapterMilestones = MilestoneAdapter(object : MilestoneAdapter.Listener {
            override fun onMilestoneClicked(milestone: Milestone) {
                Navigator.navigateToMilestone(baseActivty, project!!, milestone)
            }
        })
        layoutManagerMilestones = LinearLayoutManager(activity)
        listMilestones.layoutManager = layoutManagerMilestones
        listMilestones.addItemDecoration(DividerItemDecoration(baseActivty))
        listMilestones.adapter = adapterMilestones
        listMilestones.addOnScrollListener(mOnScrollListener)

        spinnerStates.adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, android.R.id.text1, resources.getStringArray(R.array.milestone_state_names))
        spinnerStates.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                state = states[position]
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
        App.get().gitLab.getMilestones(project!!.id, state)
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Milestone>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error_milestones)
                        adapterMilestones.setData(null)
                        nextPageUrl = null
                    }

                    override fun responseNonNullSuccess(milestones: List<Milestone>) {
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        if (milestones.isEmpty()) {
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_milestones)
                        }
                        adapterMilestones.setData(milestones)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url " + nextPageUrl)
                    }
                })
    }

    fun loadMore() {
        if (nextPageUrl == null) {
            return
        }

        loading = true
        adapterMilestones.setLoading(true)

        Timber.d("loadMore called for " + nextPageUrl!!)
        App.get().gitLab.getMilestones(nextPageUrl!!.toString())
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Milestone>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        adapterMilestones.setLoading(false)
                        loading = false
                    }

                    override fun responseNonNullSuccess(milestones: List<Milestone>) {
                        loading = false
                        adapterMilestones.setLoading(false)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapterMilestones.addData(milestones)
                    }
                })
    }

    @Subscribe
    fun onEvent(event: ProjectReloadEvent) {
        project = event.project
        loadData()
    }

    @Subscribe
    fun onEvent(event: MilestoneCreatedEvent) {
        adapterMilestones.addMilestone(event.milestone)
        textMessage.visibility = View.GONE
        listMilestones.smoothScrollToPosition(0)
    }

    @Subscribe
    fun onEvent(event: MilestoneChangedEvent) {
        adapterMilestones.updateIssue(event.milestone)
    }
}
