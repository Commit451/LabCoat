package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragment.ProjectsFragment;

/**
 * Projects Pager Adapter
 */
public class ProjectPagerAdapter extends FragmentPagerAdapter {

    private String[] titles;

    public ProjectPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        titles = context.getResources().getStringArray(R.array.projects_tabs);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return ProjectsFragment.Companion.newInstance(ProjectsFragment.Companion.getMODE_ALL());
            case 1:
                return ProjectsFragment.Companion.newInstance(ProjectsFragment.Companion.getMODE_MINE());
            case 2:
                return ProjectsFragment.Companion.newInstance(ProjectsFragment.Companion.getMODE_STARRED());
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
