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
import com.commit451.jounce.Debouncer;
import com.commit451.teleprinter.Teleprinter;

import butterknife.BindView;
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

    @BindView(R.id.root) View mRoot;
    @BindView(R.id.tabs) TabLayout mTabLayout;
    @BindView(R.id.pager) ViewPager mViewPager;
    SearchPagerAdapter mSearchPagerAdapter;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.search) EditText mSearchView;
    @BindView(R.id.clear) View mClearView;

    @OnClick(R.id.clear)
    void onClearClick() {
        mClearView.animate().alpha(0.0f).withEndAction(new Runnable() {
            @Override
            public void run() {
                mClearView.setVisibility(View.GONE);

            }
        });
        mSearchView.getText().clear();
        mTeleprinter.showKeyboard(mSearchView);
        mSearchDebouncer.cancel();
    }

    private Teleprinter mTeleprinter;

    private Debouncer<CharSequence> mSearchDebouncer = new Debouncer<CharSequence>() {
        @Override
        public void onValueSet(CharSequence value) {
            search();
        }
    };

    private final TextView.OnEditorActionListener mOnSearchEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (TextUtils.isEmpty(mSearchView.getText())) {
                mSearchView.setText("unicorns");
            }
            search();
            mTeleprinter.hideKeyboard();
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
                mSearchDebouncer.setValue(s);
            }
            //This means they are backspacing
            if (before > count) {
                Timber.d("Removing future search");
                mSearchDebouncer.cancel();
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
        mTeleprinter = new Teleprinter(this);
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
    }

    private void search() {
        Timber.d("Searching");
        mSearchPagerAdapter.searchQuery(mSearchView.getText().toString());
    }
}
