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

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.BuildSectionsPagerAdapter;
import com.commit451.gitlab.event.BuildChangedEvent;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.util.BuildUtil;
import com.commit451.gitlab.util.DownloadUtil;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
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
    ViewGroup root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.pager)
    ViewPager viewPager;
    @BindView(R.id.progress)
    View progress;

    MenuItem menuItemDownload;

    Project project;
    Build build;

    private final Toolbar.OnMenuItemClickListener mOnMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_retry:
                    progress.setVisibility(View.VISIBLE);
                    App.get().getGitLab().retryBuild(project.getId(), build.getId())
                            .compose(BuildActivity.this.<Build>bindToLifecycle())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new CustomSingleObserver<Build>() {

                                @Override
                                public void error(@NonNull Throwable t) {
                                    Timber.e(t);
                                    progress.setVisibility(View.GONE);
                                    Snackbar.make(root, R.string.unable_to_retry_build, Snackbar.LENGTH_LONG)
                                            .show();
                                }

                                @Override
                                public void success(@NonNull Build build) {
                                    progress.setVisibility(View.GONE);
                                    Snackbar.make(root, R.string.build_started, Snackbar.LENGTH_LONG)
                                            .show();
                                    App.bus().post(new BuildChangedEvent(build));
                                }
                            });
                    return true;
                case R.id.action_erase:
                    progress.setVisibility(View.VISIBLE);
                    App.get().getGitLab().eraseBuild(project.getId(), build.getId())
                            .compose(BuildActivity.this.<Build>bindToLifecycle())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new CustomSingleObserver<Build>() {

                                @Override
                                public void error(@NonNull Throwable t) {
                                    Timber.e(t);
                                    progress.setVisibility(View.GONE);
                                    Snackbar.make(root, R.string.unable_to_erase_build, Snackbar.LENGTH_LONG)
                                            .show();
                                }

                                @Override
                                public void success(@NonNull Build build) {
                                    progress.setVisibility(View.GONE);
                                    Snackbar.make(root, R.string.build_erased, Snackbar.LENGTH_LONG)
                                            .show();
                                    App.bus().post(new BuildChangedEvent(build));
                                }
                            });
                    return true;
                case R.id.action_cancel:
                    progress.setVisibility(View.VISIBLE);
                    App.get().getGitLab().cancelBuild(project.getId(), build.getId())
                            .compose(BuildActivity.this.<Build>bindToLifecycle())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new CustomSingleObserver<Build>() {

                                @Override
                                public void error(@NonNull Throwable t) {
                                    Timber.e(t);
                                    progress.setVisibility(View.GONE);
                                    Snackbar.make(root, R.string.unable_to_cancel_build, Snackbar.LENGTH_LONG)
                                            .show();
                                }

                                @Override
                                public void success(@NonNull Build build) {
                                    progress.setVisibility(View.GONE);
                                    Snackbar.make(root, R.string.build_canceled, Snackbar.LENGTH_LONG)
                                            .show();
                                    App.bus().post(new BuildChangedEvent(build));
                                }
                            });
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

        project = Parcels.unwrap(getIntent().getParcelableExtra(KEY_PROJECT));
        build = Parcels.unwrap(getIntent().getParcelableExtra(KEY_BUILD));

        toolbar.setTitle(getString(R.string.build_number) + build.getId());
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setSubtitle(project.getNameWithNamespace());
        toolbar.inflateMenu(R.menu.menu_build);
        toolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);
        menuItemDownload = toolbar.getMenu().findItem(R.id.action_download);
        menuItemDownload.setVisible(build.getArtifactsFile() != null);
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
                project,
                build);

        viewPager.setAdapter(sectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
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
        Account account = App.get().getAccount();
        String downloadUrl = BuildUtil.getDownloadBuildUrl(App.get().getAccount().getServerUrl(), project, build);
        Timber.d("Downloading build: " + downloadUrl);
        DownloadUtil.download(BuildActivity.this, account, downloadUrl, build.getArtifactsFile().getFileName());
    }
}
