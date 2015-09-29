package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragments.ProjectsFragment;
import com.commit451.gitlab.fragments.UsersFragment;

/**
 * The pager that controls the fragments when on the search activity
 * Created by Jawn on 9/21/2015.
 */
public class SearchPagerAdapter extends FragmentPagerAdapter {

    private static final int SECTION_COUNT = 2;

    private String[] mTitles;
    private ProjectsFragment mProjectsFragment;
    //TODO change this to users
    private UsersFragment mUsersFragment;

    public SearchPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mProjectsFragment = ProjectsFragment.newInstance(ProjectsFragment.MODE_SEARCH);
        mUsersFragment = UsersFragment.newInstance();
        mTitles = context.getResources().getStringArray(R.array.search_tabs);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return mProjectsFragment;
            case 1:
                return mUsersFragment;
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

    public void searchQuery(String query) {
        mProjectsFragment.searchQuery(query);
        mUsersFragment.searchQuery(query);
    }
}
