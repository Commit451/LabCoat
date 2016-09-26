package com.commit451.gitlab.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.BuildSectionsPagerAdapter;
import com.commit451.gitlab.event.BuildChangedEvent;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.BuildUtil;
import com.commit451.gitlab.util.DownloadUtil;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Shows the details of a merge request
 */
public class BuildActivity extends BaseActivity {

    private static final int REQUEST_PERMISSION_WRITE_STORAGE = 1337;

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

    MenuItem mMenuItemDownload;

    Project mProject;
    Build mBuild;

    private final EasyCallback<Build> mRetryCallback = new EasyCallback<Build>() {
        @Override
        public void success(@NonNull Build response) {
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, R.string.build_started, Snackbar.LENGTH_LONG)
                    .show();
            App.bus().post(new BuildChangedEvent(response));
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t);
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
            App.bus().post(new BuildChangedEvent(response));
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t);
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
            App.bus().post(new BuildChangedEvent(response));
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t);
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
                    App.instance().getGitLab().retryBuild(mProject.getId(), mBuild.getId()).enqueue(mRetryCallback);
                    return true;
                case R.id.action_erase:
                    mProgress.setVisibility(View.VISIBLE);
                    App.instance().getGitLab().eraseBuild(mProject.getId(), mBuild.getId()).enqueue(mEraseCallback);
                    return true;
                case R.id.action_cancel:
                    mProgress.setVisibility(View.VISIBLE);
                    App.instance().getGitLab().cancelBuild(mProject.getId(), mBuild.getId()).enqueue(mCancelCallback);
                    return true;
                case R.id.action_download:
                    checkDownloadBuild();
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
        mMenuItemDownload = mToolbar.getMenu().findItem(R.id.action_download);
        mMenuItemDownload.setVisible(mBuild.getArtifactsFile() != null);
        setupTabs();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadBuild();
                }
            }
        }
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

    @TargetApi(23)
    private void checkDownloadBuild() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            downloadBuild();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_STORAGE);
        }
    }

    private void downloadBuild() {
        Account account = App.instance().getAccount();
        String downloadUrl = BuildUtil.getDownloadBuildUrl(App.instance().getAccount().getServerUrl(), mProject, mBuild);
        Timber.d("Downloading build: " + downloadUrl);
        DownloadUtil.download(BuildActivity.this, account, downloadUrl, mBuild.getArtifactsFile().getFileName());
    }
}
