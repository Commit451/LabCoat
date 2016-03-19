package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragment.MergeRequestCommitsFragment;
import com.commit451.gitlab.fragment.MergeRequestDiscussionFragment;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Project;

/**
 * Projects Pager Adapter
 */
public class MergeRequestSectionsPagerAdapter extends FragmentPagerAdapter {
    private static final int SECTION_COUNT = 2;

    private Project mProject;
    private MergeRequest mMergeRequest;
    private String[] mTitles;

    public MergeRequestSectionsPagerAdapter(Context context, FragmentManager fm, Project project, MergeRequest mergeRequest) {
        super(fm);
        mProject = project;
        mMergeRequest = mergeRequest;
        mTitles = context.getResources().getStringArray(R.array.merge_request_tabs);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return MergeRequestDiscussionFragment.newInstance(mProject, mMergeRequest);
            case 1:
                return MergeRequestCommitsFragment.newInstance(mProject, mMergeRequest);
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
