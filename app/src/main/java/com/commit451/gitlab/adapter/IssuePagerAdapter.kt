package com.commit451.gitlab.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.commit451.gitlab.R
import com.commit451.gitlab.fragment.IssueDetailsFragment
import com.commit451.gitlab.fragment.IssueDiscussionFragment
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Project

/**
 * Issue Pager Adapter
 */
class IssuePagerAdapter(context: Context, fm: FragmentManager, private val project: Project, private val issue: Issue) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val titles: Array<String> = context.resources.getStringArray(R.array.issue_tabs)

    override fun getItem(position: Int): Fragment {

        when (position) {
            0 -> return IssueDetailsFragment.newInstance(project, issue)
            1 -> return IssueDiscussionFragment.newInstance(project, issue)
        }

        throw IllegalStateException("Position exceeded on view pager")
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }
}
