package com.commit451.gitlab.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.extension.getFeedUrl
import com.commit451.gitlab.fragment.*
import com.commit451.gitlab.model.api.Project
import timber.log.Timber
import java.util.*

/**
 * Controls the sections that should be shown in a [com.commit451.gitlab.activity.ProjectActivity]
 */
class ProjectSectionsPagerAdapter(context: ProjectActivity, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    companion object {
        val PROJECT_POS = 0
        val ACTIVITY_POS = 1
        val FILES_POS = 2
        val COMMITS_POS = 3
        val PIPELINES_POS = 4
        val BUILDS_POS = 5
        val MILESTONES_POS = 6
        val ISSUES_POS = 7
        val MERGE_REQUESTS_POS = 8
        val PROJECT_MEMBERS_POS = 9
        val SNIPPETS_POS = 10
    }

    private val project: Project = context.project!!
    private val titles: Array<String> = context.resources.getStringArray(R.array.main_tabs)
    private val disabledSections = HashSet<Int>()

    init {

        val project = context.project!!
        if (isDisabled(project.isBuildEnabled)) {
            Timber.d("Builds are disabled")
            disabledSections.add(BUILDS_POS)
            disabledSections.add(PIPELINES_POS)
        }
        if (isDisabled(project.isIssuesEnabled)) {
            Timber.d("Issues are disabled")
            disabledSections.add(ISSUES_POS)
        }
        if (isDisabled(project.isMergeRequestsEnabled)) {
            Timber.d("Merge requests are disabled")
            disabledSections.add(MERGE_REQUESTS_POS)
        }
        if (isDisabled(project.isIssuesEnabled) && isDisabled(project.isMergeRequestsEnabled)) {
            Timber.d("Milestones are disabled")
            disabledSections.add(MILESTONES_POS)
        }
        //TODO enable snippets when they are done
        if (true) {//!project.isSnippetsEnabled()) {
            Timber.d("Snippets are disabled")
            disabledSections.add(SNIPPETS_POS)
        }
    }

    override fun getCount(): Int {
        return titles.size - disabledSections.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[getCorrectPosition(position)]
    }

    override fun getItem(position: Int): Fragment {
        val correctPosition = getCorrectPosition(position)

        when (correctPosition) {
            PROJECT_POS -> return ProjectFragment.newInstance()
            ACTIVITY_POS -> return FeedFragment.newInstance(project.getFeedUrl())
            FILES_POS -> return FilesFragment.newInstance()
            COMMITS_POS -> return CommitsFragment.newInstance()
            PIPELINES_POS -> return PipelinesFragment.newInstance()
            BUILDS_POS -> return BuildsFragment.newInstance()
            MILESTONES_POS -> return MilestonesFragment.newInstance()
            ISSUES_POS -> return IssuesFragment.newInstance()
            MERGE_REQUESTS_POS -> return MergeRequestsFragment.newInstance()
            PROJECT_MEMBERS_POS -> return ProjectMembersFragment.newInstance()
            SNIPPETS_POS -> return SnippetsFragment.newInstance()
        }

        throw IllegalStateException("Position exceeded on view pager")
    }

    private fun isDisabled(enabledState: Boolean?) : Boolean{
        if (enabledState != null && !enabledState) {
            return true
        }
        return false
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
