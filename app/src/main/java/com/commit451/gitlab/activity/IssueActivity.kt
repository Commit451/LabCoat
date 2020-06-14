package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
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
import com.commit451.gitlab.util.IntentUtil
import com.commit451.teleprinter.Teleprinter
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.core.Single
import kotlinx.android.synthetic.main.activity_issue.*
import kotlinx.android.synthetic.main.progress_fullscreen.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Shows off an issue like a bar of gold
 */
class IssueActivity : BaseActivity() {

    companion object {

        private const val EXTRA_PROJECT = "extra_project"
        private const val EXTRA_SELECTED_ISSUE = "extra_selected_issue"

        fun newIntent(context: Context, project: Project, issue: Issue): Intent {
            val intent = Intent(context, IssueActivity::class.java)
            intent.putExtra(EXTRA_PROJECT, project)
            intent.putExtra(EXTRA_SELECTED_ISSUE, issue)
            return intent
        }
    }

    private lateinit var menuItemOpenClose: MenuItem
    private lateinit var teleprinter: Teleprinter

    private lateinit var project: Project
    private lateinit var issue: Issue

    private val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
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
                        .subscribe({
                            App.bus().post(IssueReloadEvent())
                            Toast.makeText(this@IssueActivity, R.string.issue_deleted, Toast.LENGTH_SHORT)
                                    .show()
                            finish()
                        }, {
                            Timber.e(it)
                            Snackbar.make(root, getString(R.string.failed_to_delete_issue), Snackbar.LENGTH_SHORT)
                                    .show()
                        })
                return@OnMenuItemClickListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue)
        teleprinter = Teleprinter(this)
        App.bus().register(this)

        project = intent.getParcelableExtra(EXTRA_PROJECT)!!
        issue = intent.getParcelableExtra(EXTRA_SELECTED_ISSUE)!!

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
        buttonEditIssue.setOnClickListener {
            val project = project
            val issue = issue
            Navigator.navigateToEditIssue(this@IssueActivity, project, issue)
        }
        bindIssue()
    }

    override fun onDestroy() {
        App.bus().unregister(this)
        super.onDestroy()
    }

    private fun bindIssue() {
        setOpenCloseMenuStatus()
        toolbar.title = getString(R.string.issue_number, issue.iid)
        toolbar.subtitle = project.nameWithNamespace
    }

    private fun closeOrOpenIssue() {
        fullscreenProgress.visibility = View.VISIBLE
        if (issue.state == Issue.STATE_CLOSED) {
            updateIssueStatus(App.get().gitLab.updateIssueStatus(project.id, issue.iid, Issue.STATE_REOPEN))
        } else {
            updateIssueStatus(App.get().gitLab.updateIssueStatus(project.id, issue.iid, Issue.STATE_CLOSE))
        }
    }

    private fun updateIssueStatus(observable: Single<Issue>) {
        observable
                .with(this)
                .subscribe({
                    fullscreenProgress.visibility = View.GONE
                    issue = it
                    App.bus().post(IssueChangedEvent(issue))
                    App.bus().post(IssueReloadEvent())
                    setOpenCloseMenuStatus()
                }, {
                    Timber.e(it)
                    fullscreenProgress.visibility = View.GONE
                    Snackbar.make(root, getString(R.string.error_changing_issue), Snackbar.LENGTH_SHORT)
                            .show()
                })
    }

    private fun setOpenCloseMenuStatus() {
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
