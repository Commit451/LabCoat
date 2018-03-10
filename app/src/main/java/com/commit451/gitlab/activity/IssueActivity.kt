package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.commit451.addendum.parceler.getParcelerParcelableExtra
import com.commit451.addendum.parceler.putParcelerParcelableExtra
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.IssuePagerAdapter
import com.commit451.gitlab.event.IssueChangedEvent
import com.commit451.gitlab.event.IssueReloadEvent
import com.commit451.gitlab.extension.getUrl
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomCompleteObserver
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.IntentUtil
import com.commit451.teleprinter.Teleprinter
import io.reactivex.Single
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Shows off an issue like a bar of gold
 */
class IssueActivity : BaseActivity() {

    companion object {

        private val EXTRA_PROJECT = "extra_project"
        private val EXTRA_SELECTED_ISSUE = "extra_selected_issue"

        fun newIntent(context: Context, project: Project, issue: Issue): Intent {
            val intent = Intent(context, IssueActivity::class.java)
            intent.putParcelerParcelableExtra(EXTRA_PROJECT, project)
            intent.putParcelerParcelableExtra(EXTRA_SELECTED_ISSUE, issue)
            return intent
        }
    }

    @BindView(R.id.root)
    lateinit var root: ViewGroup
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.tabs)
    lateinit var tabLayout: TabLayout
    @BindView(R.id.pager)
    lateinit var viewPager: ViewPager
    @BindView(R.id.progress)
    lateinit var progress: View

    lateinit var menuItemOpenClose: MenuItem
    lateinit var teleprinter: Teleprinter

    lateinit var project: Project
    lateinit var issue: Issue

    val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.action_share -> {
                IntentUtil.share(root, issue.getUrl(project))
                return@OnMenuItemClickListener true
            }
            R.id.action_close -> {
                closeOrOpenIssue()
                return@OnMenuItemClickListener true
            }
            R.id.action_delete -> {
                App.get().gitLab.deleteIssue(project.id, issue.iid)
                        .with(this)
                        .subscribe(object : CustomCompleteObserver() {

                            override fun error(t: Throwable) {
                                Timber.e(t)
                                Snackbar.make(root, getString(R.string.failed_to_delete_issue), Snackbar.LENGTH_SHORT)
                                        .show()
                            }

                            override fun complete() {
                                App.bus().post(IssueReloadEvent())
                                Toast.makeText(this@IssueActivity, R.string.issue_deleted, Toast.LENGTH_SHORT)
                                        .show()
                                finish()
                            }
                        })
                return@OnMenuItemClickListener true
            }
        }
        false
    }

    @OnClick(R.id.fab_edit_issue)
    fun onEditIssueClick() {
        val project = project
        val issue = issue
        Navigator.navigateToEditIssue(this@IssueActivity, project, issue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue)
        ButterKnife.bind(this)
        teleprinter = Teleprinter(this)
        App.bus().register(this)

        project = intent.getParcelerParcelableExtra<Project>(EXTRA_PROJECT)!!
        issue = intent.getParcelerParcelableExtra<Issue>(EXTRA_SELECTED_ISSUE)!!

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.inflateMenu(R.menu.share)
        toolbar.inflateMenu(R.menu.close)
        toolbar.inflateMenu(R.menu.delete)
        menuItemOpenClose = toolbar.menu.findItem(R.id.action_close)
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener)

        val sectionsPagerAdapter = IssuePagerAdapter(
                this,
                supportFragmentManager,
                project,
                issue)

        viewPager.adapter = sectionsPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        bindIssue()
    }

    override fun onDestroy() {
        App.bus().unregister(this)
        super.onDestroy()
    }

    fun bindIssue() {
        setOpenCloseMenuStatus()
        toolbar.title = getString(R.string.issue_number, issue.iid)
        toolbar.subtitle = project.nameWithNamespace
    }

    fun closeOrOpenIssue() {
        progress.visibility = View.VISIBLE
        if (issue.state == Issue.STATE_CLOSED) {
            updateIssueStatus(App.get().gitLab.updateIssueStatus(project.id, issue.iid, Issue.STATE_REOPEN))
        } else {
            updateIssueStatus(App.get().gitLab.updateIssueStatus(project.id, issue.iid, Issue.STATE_CLOSE))
        }
    }

    fun updateIssueStatus(observable: Single<Issue>) {
        observable
                .with(this)
                .subscribe(object : CustomSingleObserver<Issue>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        progress.visibility = View.GONE
                        Snackbar.make(root, getString(R.string.error_changing_issue), Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun success(issue: Issue) {
                        progress.visibility = View.GONE
                        this@IssueActivity.issue = issue
                        App.bus().post(IssueChangedEvent(this@IssueActivity.issue))
                        App.bus().post(IssueReloadEvent())
                        setOpenCloseMenuStatus()
                    }
                })
    }

    fun setOpenCloseMenuStatus() {
        menuItemOpenClose.setTitle(if (issue.state == Issue.STATE_CLOSED) R.string.reopen else R.string.close)
    }

    @Subscribe
    fun onEvent(event: IssueChangedEvent) {
        if (issue.id == event.issue.id) {
            issue = event.issue
            bindIssue()
        }
    }
}
