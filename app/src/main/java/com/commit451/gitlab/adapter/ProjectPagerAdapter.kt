package com.commit451.gitlab.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.commit451.gitlab.BuildConfig
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.extension.feedUrl
import com.commit451.gitlab.fragment.*
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.DeepLinker
import timber.log.Timber

/**
 * Controls the sections that should be shown in a [com.commit451.gitlab.activity.ProjectActivity]
 */
class ProjectPagerAdapter(context: ProjectActivity, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    companion object {
        const val SECTION_PROJECT = "project"
        const val SECTION_ACTIVITY = "activity"
        const val SECTION_FILES = "files"
        const val SECTION_COMMITS = "commits"
        const val SECTION_PIPELINE = "pipeline"
        const val SECTION_JOBS = "jobs"
        const val SECTION_MILESTONES = "milestones"
        const val SECTION_ISSUES = "issues"
        const val SECTION_MERGE_REQUESTS = "merge_requests"
        const val SECTION_MEMBERS = "members"
    }

    private val project: Project = context.project!!
    private val sections = mutableListOf<Section>()

    init {
        sections.add(Section(SECTION_PROJECT, context.getString(R.string.title_project)))
        sections.add(Section(SECTION_ACTIVITY, context.getString(R.string.title_activity)))
        if (!isDisabled(project.isIssuesEnabled)) {
            sections.add(Section(SECTION_ISSUES, context.getString(R.string.title_issues)))
        }
        sections.add(Section(SECTION_FILES, context.getString(R.string.title_files)))
        sections.add(Section(SECTION_COMMITS, context.getString(R.string.title_commits)))
        if (!isDisabled(project.isBuildEnabled)) {
            sections.add(Section(SECTION_PIPELINE, context.getString(R.string.title_pipelines)))
            sections.add(Section(SECTION_JOBS, context.getString(R.string.title_jobs)))
        }
        if (!isDisabled(project.isMergeRequestsEnabled)) {
            sections.add(Section(SECTION_MERGE_REQUESTS, context.getString(R.string.title_merge_requests)))
        }
        if (!isDisabled(project.isIssuesEnabled) && !isDisabled(project.isMergeRequestsEnabled)) {
            sections.add(Section(SECTION_MILESTONES, context.getString(R.string.title_milestones)))
        }
        sections.add(Section(SECTION_MEMBERS, context.getString(R.string.title_members)))
    }

    override fun getCount(): Int {
        return sections.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return sections[position].name
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        val sectionId = sections[position].id

        when (sectionId) {
            SECTION_PROJECT -> return ProjectFragment.newInstance()
            SECTION_ACTIVITY -> return FeedFragment.newInstance(project.feedUrl)
            SECTION_FILES -> return FilesFragment.newInstance()
            SECTION_COMMITS -> return CommitsFragment.newInstance()
            SECTION_PIPELINE -> return PipelinesFragment.newInstance()
            SECTION_JOBS -> return JobsFragment.newInstance()
            SECTION_MILESTONES -> return MilestonesFragment.newInstance()
            SECTION_ISSUES -> return IssuesFragment.newInstance()
            SECTION_MERGE_REQUESTS -> return MergeRequestsFragment.newInstance()
            SECTION_MEMBERS -> return ProjectMembersFragment.newInstance()
        }

        throw IllegalStateException("Nothing to do with sectionId $sectionId")
    }

    private fun isDisabled(enabledState: Boolean?): Boolean {
        if (enabledState != null && !enabledState) {
            return true
        }
        return false
    }

    fun indexForSelection(projectSelection: DeepLinker.ProjectSelection): Int {
        var index = when (projectSelection) {
            DeepLinker.ProjectSelection.PROJECT -> sections.indexOfFirst { it.id == SECTION_PROJECT }
            DeepLinker.ProjectSelection.ISSUES -> sections.indexOfFirst { it.id == SECTION_ISSUES }
            DeepLinker.ProjectSelection.COMMITS -> sections.indexOfFirst { it.id == SECTION_COMMITS }
            DeepLinker.ProjectSelection.MILESTONES -> sections.indexOfFirst { it.id == SECTION_MILESTONES }
            DeepLinker.ProjectSelection.JOBS -> sections.indexOfFirst { it.id == SECTION_JOBS }
            else -> 0
        }
        if (index == -1) {
            index = 0
        }
        return index
    }

    class Section(val id: String, val name: String)
}
