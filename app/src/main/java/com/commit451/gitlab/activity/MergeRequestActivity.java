package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.MergeRequestSectionsPagerAdapter;
import com.commit451.gitlab.event.MergeRequestChangedEvent;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Shows the details of a merge request
 */
public class MergeRequestActivity extends BaseActivity {

    private static final String KEY_PROJECT = "key_project";
    private static final String KEY_MERGE_REQUEST = "key_merge_request";

    public static Intent newIntent(Context context, Project project, MergeRequest mergeRequest) {
        Intent intent = new Intent(context, MergeRequestActivity.class);
        intent.putExtra(KEY_PROJECT, Parcels.wrap(project));
        intent.putExtra(KEY_MERGE_REQUEST, Parcels.wrap(mergeRequest));
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

    Project project;
    MergeRequest mergeRequest;

    private final Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_merge:
                    progress.setVisibility(View.VISIBLE);
                    App.get().getGitLab().acceptMergeRequest(project.getId(), mergeRequest.getId())
                            .compose(MergeRequestActivity.this.<Response<MergeRequest>>bindToLifecycle())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new CustomResponseSingleObserver<MergeRequest>() {

                                @Override
                                public void error(Throwable e) {
                                    Timber.e(e);
                                    progress.setVisibility(View.GONE);
                                    String message = getString(R.string.unable_to_merge);
                                    if (e instanceof HttpException) {
                                        int code = ((HttpException) e).response().code();
                                        if (code == 406) {
                                            message = getString(R.string.merge_request_already_merged_or_closed);
                                        }
                                    }
                                    Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                                            .show();
                                }

                                @Override
                                public void responseSuccess(MergeRequest mergeRequest) {
                                    progress.setVisibility(View.GONE);
                                    Snackbar.make(root, R.string.merge_request_accepted, Snackbar.LENGTH_LONG)
                                            .show();
                                    App.bus().post(new MergeRequestChangedEvent(mergeRequest));
                                }
                            });
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge_request);
        ButterKnife.bind(this);

        project = Parcels.unwrap(getIntent().getParcelableExtra(KEY_PROJECT));
        mergeRequest = Parcels.unwrap(getIntent().getParcelableExtra(KEY_MERGE_REQUEST));

        toolbar.setTitle(getString(R.string.merge_request_number) + mergeRequest.getIid());
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setSubtitle(project.getNameWithNamespace());
        toolbar.inflateMenu(R.menu.menu_merge_request);
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener);
        setupTabs();
    }

    private void setupTabs() {
        MergeRequestSectionsPagerAdapter sectionsPagerAdapter = new MergeRequestSectionsPagerAdapter(
                this,
                getSupportFragmentManager(),
                project,
                mergeRequest);

        viewPager.setAdapter(sectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}
