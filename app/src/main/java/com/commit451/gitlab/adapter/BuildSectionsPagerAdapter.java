package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragment.BuildLogFragment;
import com.commit451.gitlab.fragment.BuildDescriptionFragment;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;

/**
 * Build sections
 */
public class BuildSectionsPagerAdapter extends FragmentPagerAdapter {

    private Project project;
    private Build build;
    private String[] titles;

    public BuildSectionsPagerAdapter(Context context, FragmentManager fm, Project project, Build build) {
        super(fm);
        this.project = project;
        this.build = build;
        titles = context.getResources().getStringArray(R.array.build_tabs);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return BuildDescriptionFragment.newInstance(project, build);
            case 1:
                return BuildLogFragment.newInstance(project, build);
        }

        throw new IllegalStateException("Position exceeded on view pager");
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
