package com.commit451.gitlab.activity


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.adapter.MilestoneIssueAdapter
import com.commit451.gitlab.event.MilestoneChangedEvent
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Milestone
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_milestone.*
import kotlinx.android.synthetic.main.progress_fullscreen.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

class MilestoneActivity : BaseActivity() {

    companion object {

        private const val EXTRA_PROJECT = "extra_project"
        private const val EXTRA_MILESTONE = "extra_milestone"

        fun newIntent(context: Context, project: Project, milestone: Milestone): Intent {
            val intent = Intent(context, MilestoneActivity::class.java)
            intent.putExtra(EXTRA_PROJECT, project)
            intent.putExtra(EXTRA_MILESTONE, milestone)
            return intent
        }
    }

    private lateinit var adapterMilestoneIssues: MilestoneIssueAdapter
    private lateinit var layoutManagerIssues: LinearLayoutManager
    private lateinit var menuItemOpenClose: MenuItem

    private lateinit var project: Project
    private lateinit var milestone: Milestone
    private var nextPageUrl: String? = null
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
        setContentView(R.layout.activity_milestone)
        App.bus().register(this)

        project = intent.getParcelableExtra(EXTRA_PROJECT)!!
        milestone = intent.getParcelableExtra(EXTRA_MILESTONE)!!

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.inflateMenu(R.menu.close)
        menuItemOpenClose = toolbar.menu.findItem(R.id.action_close)
        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_close -> {
                    closeOrOpenIssue()
                    return@OnMenuItemClickListener true
                }
            }
            false
        })

        adapterMilestoneIssues = MilestoneIssueAdapter(object : MilestoneIssueAdapter.Listener {
            override fun onIssueClicked(issue: Issue) {
                Navigator.navigateToIssue(this@MilestoneActivity, project, issue)
            }
        })
        bind(milestone)
        listIssues.adapter = adapterMilestoneIssues
        layoutManagerIssues = LinearLayoutManager(this)
        listIssues.layoutManager = layoutManagerIssues
        listIssues.addItemDecoration(DividerItemDecoration(this))
        listIssues.addOnScrollListener(onScrollListener)
        swipeRefreshLayout.setOnRefreshListener { loadData() }

        buttonEdit.setOnClickListener {
            Navigator.navigateToEditMilestone(this@MilestoneActivity, project, milestone)
        }
        buttonAdd.setOnClickListener {
            Navigator.navigateToAddIssue(this@MilestoneActivity, buttonAdd, project)
        }
        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        App.bus().unregister(this)
    }

    fun bind(milestone: Milestone) {
        toolbar.title = milestone.title
        adapterMilestoneIssues.setMilestone(milestone)
        setOpenCloseMenuStatus()
    }

    fun loadData() {
        textMessage.visibility = View.GONE
        loading = true
        swipeRefreshLayout.isRefreshing = true
        App.get().gitLab.getMilestoneIssues(project.id, milestone.id)
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({

                    swipeRefreshLayout.isRefreshing = false
                    loading = false

                    if (it.body.isNotEmpty()) {
                        textMessage.visibility = View.GONE
                    } else {
                        Timber.d("No issues found")
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.no_issues)
                    }

                    nextPageUrl = it.paginationData.next
                    adapterMilestoneIssues.setIssues(it.body)
                }, {
                    Timber.e(it)
                    loading = false
                    swipeRefreshLayout.isRefreshing = false
                    textMessage.visibility = View.VISIBLE
                    textMessage.setText(R.string.connection_error_issues)
                    adapterMilestoneIssues.setIssues(null)
                })
    }

    fun loadMore() {

        if (nextPageUrl == null) {
            return
        }

        loading = true

        Timber.d("loadMore called for %s", nextPageUrl)
        App.get().gitLab.getMilestoneIssues(nextPageUrl!!.toString())
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    nextPageUrl = it.paginationData.next
                    adapterMilestoneIssues.addIssues(it.body)
                }, {
                    Timber.e(it)
                    loading = false
                })
    }

    private fun closeOrOpenIssue() {
        progress.visibility = View.VISIBLE
        if (milestone.state == Milestone.STATE_ACTIVE) {
            updateMilestoneStatus(App.get().gitLab.updateMilestoneStatus(project.id, milestone.id, Milestone.STATE_EVENT_CLOSE))
        } else {
            updateMilestoneStatus(App.get().gitLab.updateMilestoneStatus(project.id, milestone.id, Milestone.STATE_EVENT_ACTIVATE))
        }
    }

    private fun updateMilestoneStatus(observable: Single<Milestone>) {
        observable.with(this)
                .subscribe({
                    progress.visibility = View.GONE
                    milestone = it
                    App.bus().post(MilestoneChangedEvent(milestone))
                    setOpenCloseMenuStatus()
                }, {
                    Timber.e(it)
                    progress.visibility = View.GONE
                    Snackbar.make(root, getString(R.string.failed_to_create_milestone), Snackbar.LENGTH_SHORT)
                            .show()
                })
    }

    private fun setOpenCloseMenuStatus() {
        menuItemOpenClose.setTitle(if (milestone.state == Milestone.STATE_CLOSED) R.string.reopen else R.string.close)
    }

    @Subscribe
    fun onEvent(event: MilestoneChangedEvent) {
        if (milestone.id == event.milestone.id) {
            milestone = event.milestone
            bind(milestone)
        }
    }
}
