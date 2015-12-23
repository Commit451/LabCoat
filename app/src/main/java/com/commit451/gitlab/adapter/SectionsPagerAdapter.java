package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragment.CommitsFragment;
import com.commit451.gitlab.fragment.FilesFragment;
import com.commit451.gitlab.fragment.IssuesFragment;
import com.commit451.gitlab.fragment.ProjectMembersFragment;
import com.commit451.gitlab.fragment.MergeRequestsFragment;
import com.commit451.gitlab.fragment.OverviewFragment;

/**
 * Controls the sections that should be shown in a {@link com.commit451.gitlab.activity.ProjectActivity}
 * Created by Jawn on 9/20/2015.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final int SECTION_COUNT = 6;

    private String[] mTitles;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mTitles = context.getResources().getStringArray(R.array.main_tabs);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return OverviewFragment.newInstance();
            case 1:
                return CommitsFragment.newInstance();
            case 2:
                return IssuesFragment.newInstance();
            case 3:
                return FilesFragment.newInstance();
            case 4:
                return MergeRequestsFragment.newInstance();
            case 5:
                return ProjectMembersFragment.newInstance();
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
