package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragment.BuildArtifactsFragment;
import com.commit451.gitlab.fragment.BuildDescriptionFragment;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;

/**
 * Build sections
 */
public class BuildSectionsPagerAdapter extends FragmentPagerAdapter {
    private static final int SECTION_COUNT = 2;

    private Project mProject;
    private Build mBuild;
    private String[] mTitles;

    public BuildSectionsPagerAdapter(Context context, FragmentManager fm, Project project, Build build) {
        super(fm);
        mProject = project;
        mBuild = build;
        mTitles = context.getResources().getStringArray(R.array.build_tabs);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return BuildDescriptionFragment.newInstance(mProject, mBuild);
            case 1:
                return BuildArtifactsFragment.newInstance(mProject, mBuild);
        }

        throw new IllegalStateException("Position exceeded on view pager");
    }

    @Override
    public int getCount() {
        return SECTION_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}
