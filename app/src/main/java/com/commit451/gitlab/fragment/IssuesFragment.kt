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
import com.commit451.gitlab.adapter.IssueAdapter
import com.commit451.gitlab.event.IssueChangedEvent
import com.commit451.gitlab.event.IssueCreatedEvent
import com.commit451.gitlab.event.IssueReloadEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import org.greenrobot.eventbus.Subscribe
import retrofit2.Response
import timber.log.Timber

class IssuesFragment : ButterKnifeFragment() {

    companion object {

        fun newInstance(): IssuesFragment {
            return IssuesFragment()
        }
    }

    @BindView(R.id.root) lateinit var root: ViewGroup
    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list) lateinit var listIssues: RecyclerView
    @BindView(R.id.message_text) lateinit var textMessage: TextView
    @BindView(R.id.issue_spinner) lateinit var spinnerIssue: Spinner

    lateinit var adapterIssue: IssueAdapter
    lateinit var layoutManagerIssues: LinearLayoutManager

    var project: Project? = null
    lateinit var state: String
    lateinit var states: Array<String>
    var nextPageUrl: Uri? = null
    var loading = false

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerIssues.childCount
            val totalItemCount = layoutManagerIssues.itemCount
            val firstVisibleItem = layoutManagerIssues.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    @OnClick(R.id.add_issue_button)
    fun onAddIssueClick(fab: View) {
        if (project != null) {
            Navigator.navigateToAddIssue(activity, fab, project!!)
        } else {
            Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                    .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = resources.getString(R.string.issue_state_value_default)
        states = resources.getStringArray(R.array.issue_state_values)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_issues, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapterIssue = IssueAdapter(object : IssueAdapter.Listener {
            override fun onIssueClicked(issue: Issue) {
                if (project != null) {
                    Navigator.navigateToIssue(activity, project!!, issue)
                } else {
                    Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
        })
        layoutManagerIssues = LinearLayoutManager(activity)
        listIssues.layoutManager = layoutManagerIssues
        listIssues.addItemDecoration(DividerItemDecoration(activity))
        listIssues.adapter = adapterIssue
        listIssues.addOnScrollListener(onScrollListener)

        spinnerIssue.adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, android.R.id.text1, resources.getStringArray(R.array.issue_state_names))
        spinnerIssue.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        App.get().gitLab.getIssues(project!!.id, state)
                .setup(bindToLifecycle())
                .subscribe(object : CustomResponseSingleObserver<List<Issue>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error_issues)
                        adapterIssue.setIssues(null)
                        nextPageUrl = null
                    }

                    override fun responseSuccess(issues: List<Issue>) {
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        if (issues.isEmpty()) {
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_issues)
                        }
                        adapterIssue.setIssues(issues)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url " + nextPageUrl)
                    }
                })
    }

    fun loadMore() {
        if (nextPageUrl == null) {
            return
        }

        adapterIssue.setLoading(true)
        loading = true

        Timber.d("loadMore called for " + nextPageUrl!!)
        App.get().gitLab.getIssues(nextPageUrl!!.toString())
                .setup(bindToLifecycle())
                .subscribe(object : CustomSingleObserver<Response<List<Issue>>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        loading = false
                        adapterIssue.setLoading(false)
                    }

                    override fun success(listResponse: Response<List<Issue>>) {
                        loading = false
                        adapterIssue.setLoading(false)
                        nextPageUrl = LinkHeaderParser.parse(listResponse).next
                        adapterIssue.addIssues(listResponse.body())
                    }
                })
    }

    @Subscribe
    fun onEvent(event: ProjectReloadEvent) {
        project = event.project
        loadData()
    }

    @Subscribe
    fun onEvent(event: IssueCreatedEvent) {
        adapterIssue.addIssue(event.issue)
        if (view != null) {
            textMessage.visibility = View.GONE
            listIssues.smoothScrollToPosition(0)
        }
    }

    @Subscribe
    fun onEvent(event: IssueChangedEvent) {
        adapterIssue.updateIssue(event.issue)
    }

    @Subscribe
    fun onEvent(event: IssueReloadEvent) {
        loadData()
    }
}