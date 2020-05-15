package com.commit451.gitlab.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.feedUrl
import com.commit451.gitlab.fragment.FeedFragment
import com.commit451.gitlab.fragment.GroupMembersFragment
import com.commit451.gitlab.fragment.ProjectsFragment
import com.commit451.gitlab.model.api.Group
import java.util.*

/**
 * Group pager adapter
 */
class GroupPagerAdapter(context: Context, fm: FragmentManager, private val group: Group) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    companion object {

        private const val SECTION_COUNT = 3
        private const val ACTIVITY_POS = 0
        private const val PROJECTS_POS = 1
        private const val MEMBERS_POS = 2
    }

    private val titles: Array<String> = context.resources.getStringArray(R.array.group_tabs)
    private val disabledSections = HashSet<Int>()

    override fun getItem(position: Int): Fragment {
        when (getCorrectPosition(position)) {
            ACTIVITY_POS -> return FeedFragment.newInstance(group.feedUrl)
            PROJECTS_POS -> return ProjectsFragment.newInstance(group)
            MEMBERS_POS -> return GroupMembersFragment.newInstance(group)
        }

        throw IllegalStateException("Position exceeded on view pager")
    }

    override fun getCount(): Int {
        return SECTION_COUNT - disabledSections.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[getCorrectPosition(position)]
    }

    private fun getCorrectPosition(position: Int): Int {
        var correctPosition = position
        for (i in 0..position) {
            if (disabledSections.contains(i)) {
                correctPosition++
            }
        }

        return correctPosition
    }
}
