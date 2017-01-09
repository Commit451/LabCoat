package com.commit451.gitlab.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

import com.commit451.gitlab.R
import com.commit451.gitlab.fragment.MergeRequestCommitsFragment
import com.commit451.gitlab.fragment.MergeRequestDiscussionFragment
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project

/**
 * Projects Pager Adapter
 */
class MergeRequestSectionsPagerAdapter(context: Context, fm: FragmentManager, private val project: Project, private val mergeRequest: MergeRequest) : FragmentPagerAdapter(fm) {
    private val titles: Array<String> = context.resources.getStringArray(R.array.merge_request_tabs)

    override fun getItem(position: Int): Fragment {

        when (position) {
            0 -> return MergeRequestDiscussionFragment.newInstance(project, mergeRequest)
            1 -> return MergeRequestCommitsFragment.newInstance(project, mergeRequest)
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
