package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragment.GroupMembersFragment;
import com.commit451.gitlab.fragment.ProjectsFragment;
import com.commit451.gitlab.model.api.Group;

/**
 * Group pager adapter
 * Created by Jawn on 9/21/2015.
 */
public class GroupPagerAdapter extends FragmentPagerAdapter {
    private static final int SECTION_COUNT = 2;

    private String[] mTitles;
    private Group mGroup;

    public GroupPagerAdapter(Context context, FragmentManager fm, Group group) {
        super(fm);
        mTitles = context.getResources().getStringArray(R.array.group_tabs);
        mGroup = group;
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return ProjectsFragment.newInstance(ProjectsFragment.MODE_ALL);
            case 1:
                return GroupMembersFragment.newInstance(mGroup);
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
