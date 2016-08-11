package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragment.PickBranchFragment;
import com.commit451.gitlab.fragment.PickTagFragment;
import com.commit451.gitlab.model.Ref;

/**
 * Projects Pager Adapter
 */
public class PickBranchOrTagPagerAdapter extends FragmentPagerAdapter {

    private String[] mTitles;
    private long mProjectId;
    private Ref mCurrentRef;

    public PickBranchOrTagPagerAdapter(Context context, FragmentManager fm, long projectId, @Nullable Ref currentRef) {
        super(fm);
        mTitles = context.getResources().getStringArray(R.array.tabs_branch_tag);
        mProjectId = projectId;
        mCurrentRef = currentRef;
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return PickBranchFragment.newInstance(mProjectId, mCurrentRef);
            case 1:
                return PickTagFragment.newInstance(mProjectId, mCurrentRef);
        }

        throw new IllegalStateException("Position exceeded on view pager");
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}
