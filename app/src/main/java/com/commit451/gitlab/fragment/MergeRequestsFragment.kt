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
import com.commit451.gitlab.adapter.MergeRequestAdapter
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import kotlinx.android.synthetic.main.fragment_merge_request.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

class MergeRequestsFragment : BaseFragment() {

    companion object {

        fun newInstance(): MergeRequestsFragment {
            return MergeRequestsFragment()
        }
    }

    private lateinit var adapterMergeRequests: MergeRequestAdapter
    private lateinit var layoutManagerMergeRequests: LinearLayoutManager

    private lateinit var state: String
    private lateinit var states: Array<String>
    private var project: Project? = null
    private var nextPageUrl: String? = null
    private var loading = false

    private val onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            state = states[position]
            loadData()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
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
        state = baseActivty.resources.getString(R.string.merge_request_state_value_default)
        states = baseActivty.resources.getStringArray(R.array.merge_request_state_values)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_merge_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapterMergeRequests = MergeRequestAdapter(object : MergeRequestAdapter.Listener {
            override fun onMergeRequestClicked(mergeRequest: MergeRequest) {
                Navigator.navigateToMergeRequest(baseActivty, project!!, mergeRequest)
            }
        })
        layoutManagerMergeRequests = LinearLayoutManager(activity)
        listMergeRequests.layoutManager = layoutManagerMergeRequests
        listMergeRequests.addItemDecoration(DividerItemDecoration(baseActivty))
        listMergeRequests.adapter = adapterMergeRequests
        listMergeRequests.addOnScrollListener(onScrollListener)

        spinnerState.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, resources.getStringArray(R.array.merge_request_state_names))
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
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    swipeRefreshLayout.isRefreshing = false
                    if (it.body.isEmpty()) {
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.no_merge_requests)
                    }
                    adapterMergeRequests.setData(it.body)
                    nextPageUrl = it.paginationData.next
                    Timber.d("Next page url $nextPageUrl")
                }, {
                    loading = false
                    Timber.e(it)
                    swipeRefreshLayout.isRefreshing = false
                    textMessage.visibility = View.VISIBLE
                    textMessage.setText(R.string.connection_error_merge_requests)
                    adapterMergeRequests.setData(null)
                    nextPageUrl = null
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
        Timber.d("loadMore called for ${nextPageUrl!!}")
        App.get().gitLab.getMergeRequests(nextPageUrl!!.toString(), state)
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    adapterMergeRequests.setLoading(false)
                    nextPageUrl = it.paginationData.next
                    adapterMergeRequests.addData(it.body)
                }, {
                    Timber.e(it)
                    adapterMergeRequests.setLoading(false)
                    loading = false
                })
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
