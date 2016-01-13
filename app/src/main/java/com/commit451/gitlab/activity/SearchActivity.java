package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.SearchPagerAdapter;
import com.commit451.gitlab.util.KeyboardUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Search for :allthethings:
 */
public class SearchActivity extends BaseActivity {

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        return intent;
    }

    @Bind(R.id.root) View mRoot;
    @Bind(R.id.tabs) TabLayout mTabLayout;
    @Bind(R.id.pager) ViewPager mViewPager;
    SearchPagerAdapter mSearchPagerAdapter;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.search) EditText mSearchView;
    @Bind(R.id.clear) View mClearView;

    @OnClick(R.id.clear)
    void onClearClick() {
        mClearView.animate().alpha(0.0f).withEndAction(new Runnable() {
            @Override
            public void run() {
                mClearView.setVisibility(View.GONE);
                mSearchView.getText().clear();
            }
        });
    }

    private SearchDebouncer mSearchDebouncer;

    private final TextView.OnEditorActionListener mOnSearchEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (TextUtils.isEmpty(mSearchView.getText())) {
                mSearchView.setText("unicorns");
            }
            search();
            KeyboardUtil.hideKeyboard(SearchActivity.this);
            mRoot.removeCallbacks(mSearchDebouncer);
            return false;
        }
    };

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(s)) {
                mClearView.animate().alpha(0.0f).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mClearView.setVisibility(View.GONE);
                    }
                });
            } else if (count == 1) {
                mClearView.setVisibility(View.VISIBLE);
                mClearView.animate().alpha(1.0f);
            }
            if (s != null &&  s.length() > 3) {
                Timber.d("Posting new future search");
                mRoot.removeCallbacks(mSearchDebouncer);
                mRoot.postDelayed(mSearchDebouncer, 500);
            }
            //This means they are backspacing
            if (before > count) {
                Timber.d("Removing future search");
                mRoot.removeCallbacks(mSearchDebouncer);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
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
        mSearchView.addTextChangedListener(mTextWatcher);
        mSearchDebouncer = new SearchDebouncer();
    }

    private void search() {
        Timber.d("Searching");
        mSearchPagerAdapter.searchQuery(mSearchView.getText().toString());
    }

    private class SearchDebouncer implements Runnable{
        @Override
        public void run() {
            search();
        }
    }
}
