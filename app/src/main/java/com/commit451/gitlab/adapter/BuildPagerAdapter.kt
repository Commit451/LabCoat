package com.commit451.gitlab.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.commit451.gitlab.R
import com.commit451.gitlab.fragment.BuildDescriptionFragment
import com.commit451.gitlab.fragment.BuildLogFragment
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.model.api.Project

/**
 * Build sections
 */
class BuildPagerAdapter(context: Context, fm: FragmentManager, private val project: Project, private val build: Build) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val titles: Array<String> = context.resources.getStringArray(R.array.build_tabs)

    override fun getItem(position: Int): Fragment {

        when (position) {
            0 -> return BuildDescriptionFragment.newInstance(project, build)
            1 -> return BuildLogFragment.newInstance(project, build)
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
