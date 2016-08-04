package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.fragment.TodoFragment;

/**
 * Projects Pager Adapter
 */
public class TodoPagerAdapter extends FragmentPagerAdapter {
    private static final int SECTION_COUNT = 2;

    private String[] mTitles;

    public TodoPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mTitles = context.getResources().getStringArray(R.array.tabs_todo);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return TodoFragment.newInstance(TodoFragment.MODE_TODO);
            case 1:
                return TodoFragment.newInstance(TodoFragment.MODE_DONE);
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
