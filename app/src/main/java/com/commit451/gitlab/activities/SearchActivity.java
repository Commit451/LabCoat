package com.commit451.gitlab.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.SearchPagerAdapter;
import com.commit451.gitlab.tools.KeyboardUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Search for :allthethings:
 * Created by Jawn on 9/21/2015.
 */
public class SearchActivity extends BaseActivity {

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        return intent;
    }

    @Bind(R.id.tabs) TabLayout mTabLayout;
    @Bind(R.id.pager) ViewPager mViewPager;
    SearchPagerAdapter mSearchPagerAdapter;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.search) EditText mSearchView;

    private final TextView.OnEditorActionListener mOnSearchEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (TextUtils.isEmpty(mSearchView.getText())) {
                mSearchView.setText("unicorns");
            }
            mSearchPagerAdapter.searchQuery(mSearchView.getText().toString());
            KeyboardUtil.hideKeyboard(SearchActivity.this);
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mSearchPagerAdapter = new SearchPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mSearchPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mSearchView.setOnEditorActionListener(mOnSearchEditorActionListener);
    }
}
