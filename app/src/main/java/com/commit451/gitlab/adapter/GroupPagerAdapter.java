package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragments.ProjectsFragment;

/**
 * Group pager adapter
 * Created by Jawn on 9/21/2015.
 */
public class GroupPagerAdapter extends FragmentPagerAdapter {
    private static final int SECTION_COUNT = 2;

    private String[] mTitles;

    public GroupPagerAdapter(Context context, FragmentManager fm) {
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
