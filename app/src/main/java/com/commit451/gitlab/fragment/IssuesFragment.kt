package com.commit451.gitlab.fragment

import android.net.Uri
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
import com.commit451.gitlab.adapter.IssueAdapter
import com.commit451.gitlab.event.IssueChangedEvent
import com.commit451.gitlab.event.IssueCreatedEvent
import com.commit451.gitlab.event.IssueReloadEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_issues.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

class IssuesFragment : BaseFragment() {

    companion object {

        fun newInstance(): IssuesFragment {
            return IssuesFragment()
        }
    }

    private lateinit var adapterIssue: IssueAdapter
    private lateinit var layoutManagerIssues: LinearLayoutManager

    private var project: Project? = null
    private lateinit var state: String
    private lateinit var states: Array<String>
    private var nextPageUrl: Uri? = null
    private var loading = false

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerIssues.childCount
            val totalItemCount = layoutManagerIssues.itemCount
            val firstVisibleItem = layoutManagerIssues.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

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
        adapterIssue = IssueAdapter(object : IssueAdapter.Listener {
            override fun onIssueClicked(issue: Issue) {
                if (project != null) {
                    Navigator.navigateToIssue(baseActivty, project!!, issue)
                } else {
                    Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
        })
        layoutManagerIssues = LinearLayoutManager(activity)
        listIssues.layoutManager = layoutManagerIssues
        listIssues.addItemDecoration(DividerItemDecoration(baseActivty))
        listIssues.adapter = adapterIssue
        listIssues.addOnScrollListener(onScrollListener)

        spinnerIssue.adapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, resources.getStringArray(R.array.issue_state_names))
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
                .with(this)
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

                    override fun responseNonNullSuccess(issues: List<Issue>) {
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        if (issues.isEmpty()) {
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_issues)
                        }
                        adapterIssue.setIssues(issues)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url $nextPageUrl")
                    }
                })
    }

    fun loadMore() {
        if (nextPageUrl == null) {
            return
        }

        adapterIssue.setLoading(true)
        loading = true

        Timber.d("loadMore called for ${nextPageUrl!!}")
        App.get().gitLab.getIssues(nextPageUrl!!.toString())
                .with(this)
                .subscribe({
                    loading = false
                    adapterIssue.setLoading(false)
                    nextPageUrl = LinkHeaderParser.parse(it).next
                    adapterIssue.addIssues(it.body())
                }, {
                    Timber.e(it)
                    loading = false
                    adapterIssue.setLoading(false)
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

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onEvent(event: IssueReloadEvent) {
        loadData()
    }
}
