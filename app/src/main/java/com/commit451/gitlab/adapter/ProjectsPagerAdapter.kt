package com.commit451.gitlab.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.commit451.gitlab.R
import com.commit451.gitlab.fragment.ProjectsFragment

/**
 * Projects Pager Adapter
 */
class ProjectsPagerAdapter(context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val titles: Array<String> = context.resources.getStringArray(R.array.projects_tabs)

    override fun getItem(position: Int): Fragment {

        when (position) {
            0 -> return ProjectsFragment.newInstance(ProjectsFragment.MODE_ALL)
            1 -> return ProjectsFragment.newInstance(ProjectsFragment.MODE_MINE)
            2 -> return ProjectsFragment.newInstance(ProjectsFragment.MODE_STARRED)
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
