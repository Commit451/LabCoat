package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragment.ProjectsFragment;
import com.commit451.gitlab.fragment.UsersFragment;

/**
 * The pager that controls the fragments when on the search activity
 */
public class SearchPagerAdapter extends FragmentPagerAdapter {

    private String[] titles;

    private ProjectsFragment projectsFragment;
    private UsersFragment usersFragment;

    public SearchPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        projectsFragment = ProjectsFragment.newInstance(ProjectsFragment.MODE_SEARCH);
        usersFragment = UsersFragment.newInstance();
        titles = context.getResources().getStringArray(R.array.search_tabs);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return projectsFragment;
            case 1:
                return usersFragment;
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

    public void searchQuery(String query) {
        projectsFragment.searchQuery(query);
        usersFragment.searchQuery(query);
    }
}
