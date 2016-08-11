package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.PickBranchOrTagPagerAdapter;
import com.commit451.gitlab.model.Ref;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Intermediate activity when deep linking to another activity and things need to load
 */
public class PickBranchOrTagActivity extends AppCompatActivity {

    private static final String EXTRA_PROJECT_ID = "project_id";
    private static final String EXTRA_CURRENT_REF = "current_ref";

    public static final String EXTRA_REF = "ref";

    public static Intent newIntent(Context context, long projectId, @Nullable Ref currentRef) {
        Intent intent = new Intent(context, PickBranchOrTagActivity.class);
        intent.putExtra(EXTRA_PROJECT_ID, projectId);
        intent.putExtra(EXTRA_CURRENT_REF, Parcels.wrap(currentRef));
        return intent;
    }

    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.pager)
    ViewPager mViewPager;

    @OnClick(R.id.root)
    void onRootClicked() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_branch_or_tag);
        ButterKnife.bind(this);
        long projectId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
        Ref currentRef = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_CURRENT_REF));
        mViewPager.setAdapter(new PickBranchOrTagPagerAdapter(this, getSupportFragmentManager(), projectId, currentRef));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.do_nothing, R.anim.fade_out);
    }
}
