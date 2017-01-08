package com.commit451.gitlab.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.fragment.BuildsFragment;
import com.commit451.gitlab.fragment.CommitsFragment;
import com.commit451.gitlab.fragment.FeedFragment;
import com.commit451.gitlab.fragment.FilesFragment;
import com.commit451.gitlab.fragment.IssuesFragment;
import com.commit451.gitlab.fragment.MilestonesFragment;
import com.commit451.gitlab.fragment.ProjectMembersFragment;
import com.commit451.gitlab.fragment.MergeRequestsFragment;
import com.commit451.gitlab.fragment.ProjectFragment;
import com.commit451.gitlab.fragment.SnippetsFragment;
import com.commit451.gitlab.model.api.Project;

import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

/**
 * Controls the sections that should be shown in a {@link com.commit451.gitlab.activity.ProjectActivity}
 */
public class ProjectSectionsPagerAdapter extends FragmentPagerAdapter {

    private static final int PROJECT_POS = 0;
    private static final int ACTIVITY_POS = 1;
    private static final int FILES_POS = 2;
    private static final int COMMITS_POS = 3;
    private static final int BUILDS_POS = 4;
    private static final int MILESTONES_POS = 5;
    private static final int ISSUES_POS = 6;
    private static final int MERGE_REQUESTS_POS = 7;
    private static final int PROJECT_MEMBERS_POS = 8;
    private static final int SNIPPETS_POS = 9;

    private final Project project;
    private final String[] titles;
    private final Set<Integer> disabledSections = new HashSet<>();

    public ProjectSectionsPagerAdapter(ProjectActivity context, FragmentManager fm) {
        super(fm);

        project = context.getProject();
        titles = context.getResources().getStringArray(R.array.main_tabs);

        Project project = context.getProject();
        if (!project.isBuildEnabled()) {
            Timber.d("Builds are disabled");
            disabledSections.add(BUILDS_POS);
        }
        if (!project.isIssuesEnabled()) {
            Timber.d("Issues are disabled");
            disabledSections.add(ISSUES_POS);
        }
        if (!project.isMergeRequestsEnabled()) {
            Timber.d("Merge requests are disabled");
            disabledSections.add(MERGE_REQUESTS_POS);
        }
        if (!project.isIssuesEnabled() && !project.isMergeRequestsEnabled()) {
            Timber.d("Milestones are disabled");
            disabledSections.add(MILESTONES_POS);
        }
        //TODO enable snippets when they are done
        if (true){//!project.isSnippetsEnabled()) {
            Timber.d("Snippets are disabled");
            disabledSections.add(SNIPPETS_POS);
        }
    }

    @Override
    public int getCount() {
        return titles.length - disabledSections.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        position = getCorrectPosition(position);

        return titles[position];
    }

    @Override
    public Fragment getItem(int position) {
        position = getCorrectPosition(position);

        switch (position) {
            case PROJECT_POS:
                return ProjectFragment.newInstance();
            case ACTIVITY_POS:
                return FeedFragment.newInstance(project.getFeedUrl());
            case FILES_POS:
                return FilesFragment.newInstance();
            case COMMITS_POS:
                return CommitsFragment.newInstance();
            case BUILDS_POS:
                return BuildsFragment.newInstance();
            case MILESTONES_POS:
                return MilestonesFragment.newInstance();
            case ISSUES_POS:
                return IssuesFragment.newInstance();
            case MERGE_REQUESTS_POS:
                return MergeRequestsFragment.newInstance();
            case PROJECT_MEMBERS_POS:
                return ProjectMembersFragment.newInstance();
            case SNIPPETS_POS:
                return SnippetsFragment.newInstance();
        }

        throw new IllegalStateException("Position exceeded on view pager");
    }

    private int getCorrectPosition(int position) {
        for (int i = 0; i <= position; i++) {
            if (disabledSections.contains(i)) {
                position++;
            }
        }

        return position;
    }
}
