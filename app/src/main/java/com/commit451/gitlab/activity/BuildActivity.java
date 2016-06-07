package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.BuildSectionsPagerAdapter;
import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.BuildChangedEvent;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Shows the details of a merge request
 */
public class BuildActivity extends BaseActivity {

    private static final String KEY_PROJECT = "key_project";
    private static final String KEY_BUILD = "key_merge_request";

    public static Intent newIntent(Context context, Project project, Build build) {
        Intent intent = new Intent(context, BuildActivity.class);
        intent.putExtra(KEY_PROJECT, Parcels.wrap(project));
        intent.putExtra(KEY_BUILD, Parcels.wrap(build));
        return intent;
    }

    @BindView(R.id.root)
    ViewGroup mRoot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.pager)
    ViewPager mViewPager;
    @BindView(R.id.progress)
    View mProgress;

    Project mProject;
    Build mBuild;

    private final EasyCallback<Build> mRetryCallback = new EasyCallback<Build>() {
        @Override
        public void success(@NonNull Build response) {
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, R.string.build_started, Snackbar.LENGTH_LONG)
                    .show();
            LabCoatApp.bus().post(new BuildChangedEvent(response));
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, R.string.unable_to_retry_build, Snackbar.LENGTH_LONG)
                    .show();
        }
    };

    private final EasyCallback<Build> mEraseCallback = new EasyCallback<Build>() {
        @Override
        public void success(@NonNull Build response) {
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, R.string.build_erased, Snackbar.LENGTH_LONG)
                    .show();
            LabCoatApp.bus().post(new BuildChangedEvent(response));
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, R.string.unable_to_erase_build, Snackbar.LENGTH_LONG)
                    .show();
        }
    };

    private final EasyCallback<Build> mCancelCallback = new EasyCallback<Build>() {
        @Override
        public void success(@NonNull Build response) {
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, R.string.build_canceled, Snackbar.LENGTH_LONG)
                    .show();
            LabCoatApp.bus().post(new BuildChangedEvent(response));
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, R.string.unable_to_cancel_build, Snackbar.LENGTH_LONG)
                    .show();
        }
    };

    private final Toolbar.OnMenuItemClickListener mOnMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_retry:
                    mProgress.setVisibility(View.VISIBLE);
                    GitLabClient.instance().retryBuild(mProject.getId(), mBuild.getId()).enqueue(mRetryCallback);
                    return true;
                case R.id.action_erase:
                    mProgress.setVisibility(View.VISIBLE);
                    GitLabClient.instance().eraseBuild(mProject.getId(), mBuild.getId()).enqueue(mEraseCallback);
                    return true;
                case R.id.action_cancel:
                    mProgress.setVisibility(View.VISIBLE);
                    GitLabClient.instance().cancelBuild(mProject.getId(), mBuild.getId()).enqueue(mCancelCallback);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build);
        ButterKnife.bind(this);

        mProject = Parcels.unwrap(getIntent().getParcelableExtra(KEY_PROJECT));
        mBuild = Parcels.unwrap(getIntent().getParcelableExtra(KEY_BUILD));

        mToolbar.setTitle(getString(R.string.build_number) + mBuild.getId());
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setSubtitle(mProject.getNameWithNamespace());
        mToolbar.inflateMenu(R.menu.menu_build);
        mToolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);
        setupTabs();
    }

    private void setupTabs() {
        BuildSectionsPagerAdapter sectionsPagerAdapter = new BuildSectionsPagerAdapter(
                this,
                getSupportFragmentManager(),
                mProject,
                mBuild);

        mViewPager.setAdapter(sectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }
}
