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

    private String[] titles;
    private long projectId;
    private Ref ref;

    public PickBranchOrTagPagerAdapter(Context context, FragmentManager fm, long projectId, @Nullable Ref currentRef) {
        super(fm);
        titles = context.getResources().getStringArray(R.array.tabs_branch_tag);
        this.projectId = projectId;
        ref = currentRef;
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return PickBranchFragment.newInstance(projectId, ref);
            case 1:
                return PickTagFragment.newInstance(projectId, ref);
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
