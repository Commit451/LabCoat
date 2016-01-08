package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragment.FeedFragment;
import com.commit451.gitlab.fragment.GroupMembersFragment;
import com.commit451.gitlab.fragment.ProjectsFragment;
import com.commit451.gitlab.model.api.Group;

import java.util.HashSet;
import java.util.Set;

/**
 * Group pager adapter
 * Created by Jawn on 9/21/2015.
 */
public class GroupPagerAdapter extends FragmentPagerAdapter {

    private static final int SECTION_COUNT = 3;
    private static final int ACTIVITY_POS = 0;
    private static final int PROJECTS_POS = 1;
    private static final int MEMBERS_POS = 2;

    private final Group mGroup;
    private final String[] mTitles;
    private final Set<Integer> mDisabledSections = new HashSet<>();

    public GroupPagerAdapter(Context context, FragmentManager fm, Group group) {
        super(fm);

        mGroup = group;
        mTitles = context.getResources().getStringArray(R.array.group_tabs);
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
            case ACTIVITY_POS:
                return FeedFragment.newInstance(mGroup.getFeedUrl().toString());
            case PROJECTS_POS:
                return ProjectsFragment.newInstance(mGroup);
            case MEMBERS_POS:
                return GroupMembersFragment.newInstance(mGroup);
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
