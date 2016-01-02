package com.commit451.gitlab.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.fragment.CommitsFragment;
import com.commit451.gitlab.fragment.FeedFragment;
import com.commit451.gitlab.fragment.FilesFragment;
import com.commit451.gitlab.fragment.IssuesFragment;
import com.commit451.gitlab.fragment.MilestonesFragment;
import com.commit451.gitlab.fragment.ProjectMembersFragment;
import com.commit451.gitlab.fragment.MergeRequestsFragment;
import com.commit451.gitlab.fragment.OverviewFragment;
import com.commit451.gitlab.model.api.Project;

import java.util.HashSet;
import java.util.Set;

/**
 * Controls the sections that should be shown in a {@link com.commit451.gitlab.activity.ProjectActivity}
 * Created by Jawn on 9/20/2015.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final int SECTION_COUNT = 7;
    private static final int OVERVIEW_POS = 0;
    private static final int ACTIVITY_POS = 1;
    private static final int FILES_POS = 2;
    private static final int COMMITS_POS = 3;
    private static final int MILESTONES_POS = 4;
    private static final int ISSUES_POS = 5;
    private static final int MERGE_REQUESTS_POS = 6;
    private static final int PROJECT_MEMBERS_POS = 7;

    private final Project mProject;
    private final String[] mTitles;
    private final Set<Integer> mDisabledSections = new HashSet<>();

    public SectionsPagerAdapter(ProjectActivity context, FragmentManager fm) {
        super(fm);

        mProject = context.getProject();
        mTitles = context.getResources().getStringArray(R.array.main_tabs);

        Project project = context.getProject();
        if (!project.isIssuesEnabled()) {
            mDisabledSections.add(ISSUES_POS);
        }
        if (!project.isMergeRequestsEnabled()) {
            mDisabledSections.add(MERGE_REQUESTS_POS);
        }
    }

    @Override
    public int getCount() {
        return SECTION_COUNT - mDisabledSections.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        position = getCorrectPosition(position);

        return mTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        position = getCorrectPosition(position);

        switch (position) {
            case OVERVIEW_POS:
                return OverviewFragment.newInstance();
            case ACTIVITY_POS:
                return FeedFragment.newInstance(mProject.getFeedUrl().toString());
            case FILES_POS:
                return FilesFragment.newInstance();
            case COMMITS_POS:
                return CommitsFragment.newInstance();
            case MILESTONES_POS:
                return MilestonesFragment.newInstance();
            case ISSUES_POS:
                return IssuesFragment.newInstance();
            case MERGE_REQUESTS_POS:
                return MergeRequestsFragment.newInstance();
            case PROJECT_MEMBERS_POS:
                return ProjectMembersFragment.newInstance();
        }

        throw new IllegalStateException("Position exceeded on view pager");
    }

    private int getCorrectPosition(int position) {
        for (int i = 0; i <= position; i++) {
            if (mDisabledSections.contains(i)) {
                position++;
            }
        }

        return position;
    }
}
