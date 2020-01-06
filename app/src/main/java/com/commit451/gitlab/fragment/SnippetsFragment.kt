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
import com.commit451.gitlab.adapter.SnippetAdapter
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.Snippet
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_snippets.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

class SnippetsFragment : BaseFragment() {

    companion object {

        fun newInstance(): SnippetsFragment {
            return SnippetsFragment()
        }
    }

    private lateinit var adapterSnippets: SnippetAdapter
    private lateinit var layoutManagerSnippets: LinearLayoutManager

    private lateinit var state: String
    private lateinit var states: Array<String>
    private var project: Project? = null
    private var loading = false
    private var nextPageUrl: Uri? = null

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerSnippets.childCount
            val totalItemCount = layoutManagerSnippets.itemCount
            val firstVisibleItem = layoutManagerSnippets.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = resources.getString(R.string.milestone_state_value_default)
        states = resources.getStringArray(R.array.milestone_state_values)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_snippets, container, false)
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
        adapterSnippets = SnippetAdapter(object : SnippetAdapter.Listener {
            override fun onSnippetClicked(snippet: Snippet) {

            }
        })
        layoutManagerSnippets = LinearLayoutManager(activity)
        listSnippets.layoutManager = layoutManagerSnippets
        listSnippets.addItemDecoration(DividerItemDecoration(baseActivty))
        listSnippets.adapter = adapterSnippets
        listSnippets.addOnScrollListener(onScrollListener)

        spinnerState.adapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, resources.getStringArray(R.array.milestone_state_names))
        spinnerState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                state = states[position]
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
        App.get().gitLab.getSnippets(project!!.id)
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Snippet>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error_milestones)
                        adapterSnippets.setData(null)
                        nextPageUrl = null
                    }

                    override fun responseNonNullSuccess(snippets: List<Snippet>) {
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        if (snippets.isEmpty()) {
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_milestones)
                        }
                        adapterSnippets.setData(snippets)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url %s", nextPageUrl)
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

        loading = true
        adapterSnippets.setLoading(true)

        Timber.d("loadMore called for %s", nextPageUrl)
        App.get().gitLab.getSnippets(nextPageUrl!!.toString())
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Snippet>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        adapterSnippets.setLoading(false)
                        loading = false
                    }

                    override fun responseNonNullSuccess(snippets: List<Snippet>) {
                        loading = false
                        adapterSnippets.setLoading(false)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapterSnippets.addData(snippets)
                    }
                })
    }

    @Suppress("unused")
    @Subscribe
    fun onProjectReload(event: ProjectReloadEvent) {
        project = event.project
        loadData()
    }
}
