package com.commit451.gitlab.fragment

import android.net.Uri
import android.os.Bundle
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
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.adapter.MergeRequestAdapter
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

class MergeRequestsFragment : ButterKnifeFragment() {

    companion object {

        fun newInstance(): MergeRequestsFragment {
            return MergeRequestsFragment()
        }
    }

    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list) lateinit var listMergeRequests: RecyclerView
    @BindView(R.id.message_text) lateinit var textMessage: TextView
    @BindView(R.id.state_spinner) lateinit var spinnerState: Spinner

    lateinit var adapterMergeRequests: MergeRequestAdapter
    lateinit var layoutManagerMergeRequests: LinearLayoutManager

    lateinit var state: String
    lateinit var states: Array<String>
    var project: Project? = null
    var nextPageUrl: Uri? = null
    var loading = false

    val onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            state = states[position]
            loadData()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerMergeRequests.childCount
            val totalItemCount = layoutManagerMergeRequests.itemCount
            val firstVisibleItem = layoutManagerMergeRequests.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = context.resources.getString(R.string.merge_request_state_value_default)
        states = context.resources.getStringArray(R.array.merge_request_state_values)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_merge_request, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapterMergeRequests = MergeRequestAdapter(object : MergeRequestAdapter.Listener {
            override fun onMergeRequestClicked(mergeRequest: MergeRequest) {
                Navigator.navigateToMergeRequest(activity, project!!, mergeRequest)
            }
        })
        layoutManagerMergeRequests = LinearLayoutManager(activity)
        listMergeRequests.layoutManager = layoutManagerMergeRequests
        listMergeRequests.addItemDecoration(DividerItemDecoration(activity))
        listMergeRequests.adapter = adapterMergeRequests
        listMergeRequests.addOnScrollListener(onScrollListener)

        spinnerState.adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, android.R.id.text1, resources.getStringArray(R.array.merge_request_state_names))
        spinnerState.onItemSelectedListener = onItemSelectedListener

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
        if (view == null) {
            return
        }
        if (project == null) {
            swipeRefreshLayout.isRefreshing = false
            return
        }
        textMessage.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true
        nextPageUrl = null
        loading = true
        App.get().gitLab.getMergeRequests(project!!.id, state)
                .setup(bindToLifecycle())
                .subscribe(object : CustomResponseSingleObserver<List<MergeRequest>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error_merge_requests)
                        adapterMergeRequests.setData(null)
                        nextPageUrl = null
                    }

                    override fun responseSuccess(mergeRequests: List<MergeRequest>) {
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        if (mergeRequests.isEmpty()) {
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_merge_requests)
                        }
                        adapterMergeRequests.setData(mergeRequests)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url " + nextPageUrl)
                    }
                })
    }

    fun loadMore() {
        if (view == null) {
            return
        }
        if (nextPageUrl == null) {
            return
        }
        adapterMergeRequests.setLoading(true)
        loading = true
        Timber.d("loadMore called for " + nextPageUrl!!)
        App.get().gitLab.getMergeRequests(nextPageUrl!!.toString(), state)
                .setup(bindToLifecycle())
                .subscribe(object : CustomResponseSingleObserver<List<MergeRequest>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        adapterMergeRequests.setLoading(false)
                        loading = false
                    }

                    override fun responseSuccess(mergeRequests: List<MergeRequest>) {
                        loading = false
                        adapterMergeRequests.setLoading(false)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapterMergeRequests.addData(mergeRequests)
                    }
                })
    }

    @Subscribe
    fun onProjectReload(event: ProjectReloadEvent) {
        project = event.project
        loadData()
    }

    @Subscribe
    fun onMergeRequestChanged(event: MergeRequestChangedEvent) {
        loadData()
    }
}
