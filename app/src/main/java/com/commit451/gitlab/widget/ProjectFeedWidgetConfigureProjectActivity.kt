package com.commit451.gitlab.widget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.BaseActivity
import com.commit451.gitlab.adapter.ProjectsPagerAdapter
import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.api.GitLabFactory
import com.commit451.gitlab.api.GitLabService
import com.commit451.gitlab.api.OkHttpClientFactory
import com.commit451.gitlab.fragment.ProjectsFragment
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.model.api.Project
import kotlinx.android.synthetic.main.activity_project_feed_widget_configure.*

/**
 * You chose your account, now choose your project!
 */
class ProjectFeedWidgetConfigureProjectActivity : BaseActivity(), ProjectsFragment.Listener {

    companion object {

        const val EXTRA_PROJECT = "project"
        const val EXTRA_ACCOUNT = "account"

        fun newIntent(context: Context, account: Account): Intent {
            val intent = Intent(context, ProjectFeedWidgetConfigureProjectActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT, account)
            return intent
        }
    }

    private lateinit var gitLabInstance: GitLab

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_feed_widget_configure)

        val account = intent.getParcelableExtra<Account>(EXTRA_ACCOUNT)!!
        gitLabInstance = GitLabFactory.createGitLab(account, OkHttpClientFactory.create(account, false))

        viewPager.adapter = ProjectsPagerAdapter(this, supportFragmentManager)
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onProjectClicked(project: Project) {
        val data = Intent()
        data.putExtra(EXTRA_PROJECT, project)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun providedGitLab(): GitLab {
        return gitLabInstance
    }
}
