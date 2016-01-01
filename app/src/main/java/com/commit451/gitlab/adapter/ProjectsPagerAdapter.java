package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragment.ProjectsFragment;

/**
 * Projects Pager Adapter
 * Created by Jawn on 9/21/2015.
 */
public class ProjectsPagerAdapter extends FragmentPagerAdapter {
    private static final int SECTION_COUNT = 3;

    private String[] mTitles;

    public ProjectsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mTitles = context.getResources().getStringArray(R.array.projects_tabs);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return ProjectsFragment.newInstance(ProjectsFragment.MODE_ALL);
            case 1:
                return ProjectsFragment.newInstance(ProjectsFragment.MODE_MINE);
            case 2:
                return ProjectsFragment.newInstance(ProjectsFragment.MODE_STARRED);
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
