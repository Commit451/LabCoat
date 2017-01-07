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

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        return intent;
    }

    @BindView(R.id.root)
    View root;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.pager)
    ViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search)
    EditText textSearch;
    @BindView(R.id.clear)
    View buttonClear;

    SearchPagerAdapter adapterSearch;

    @OnClick(R.id.clear)
    void onClearClick() {
        buttonClear.animate().alpha(0.0f).withEndAction(new Runnable() {
            @Override
            public void run() {
                buttonClear.setVisibility(View.GONE);

            }
        });
        textSearch.getText().clear();
        mTeleprinter.showKeyboard(textSearch);
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
            if (TextUtils.isEmpty(textSearch.getText())) {
                textSearch.setText("unicorns");
            }
            search();
            mTeleprinter.hideKeyboard();
            return false;
        }
    };

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(s)) {
                buttonClear.animate().alpha(0.0f).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        buttonClear.setVisibility(View.GONE);
                    }
                });
            } else if (count == 1) {
                buttonClear.setVisibility(View.VISIBLE);
                buttonClear.animate().alpha(1.0f);
            }
            if (s != null && s.length() > 3) {
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
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        mTeleprinter = new Teleprinter(this);
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        adapterSearch = new SearchPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapterSearch);
        tabLayout.setupWithViewPager(viewPager);
        textSearch.setOnEditorActionListener(mOnSearchEditorActionListener);
        textSearch.addTextChangedListener(mTextWatcher);
    }

    private void search() {
        Timber.d("Searching");
        adapterSearch.searchQuery(textSearch.getText().toString());
    }
}
