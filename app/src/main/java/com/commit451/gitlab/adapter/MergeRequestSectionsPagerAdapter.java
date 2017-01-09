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

    private Project project;
    private MergeRequest mergeRequest;
    private String[] titles;

    public MergeRequestSectionsPagerAdapter(Context context, FragmentManager fm, Project project, MergeRequest mergeRequest) {
        super(fm);
        this.project = project;
        this.mergeRequest = mergeRequest;
        titles = context.getResources().getStringArray(R.array.merge_request_tabs);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return MergeRequestDiscussionFragment.Companion.newInstance(project, mergeRequest);
            case 1:
                return MergeRequestCommitsFragment.Companion.newInstance(project, mergeRequest);
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
