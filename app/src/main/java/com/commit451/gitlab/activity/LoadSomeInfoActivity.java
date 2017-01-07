package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomSingleObserver;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Intermediate activity when deep linking to another activity and things need to load
 */
public class LoadSomeInfoActivity extends BaseActivity {

    private static final String EXTRA_LOAD_TYPE = "load_type";
    private static final String EXTRA_PROJECT_NAMESPACE = "project_namespace";
    private static final String EXTRA_PROJECT_NAME = "project_name";
    private static final String EXTRA_COMMIT_SHA = "extra_commit_sha";
    private static final String EXTRA_MERGE_REQUEST = "merge_request";
    private static final String EXTRA_BUILD_ID = "build_id";
    private static final String EXTRA_MILESTONE_ID = "milestone_id";

    private static final int LOAD_TYPE_DIFF = 0;
    private static final int LOAD_TYPE_MERGE_REQUEST = 1;
    private static final int LOAD_TYPE_BUILD = 2;
    private static final int LOAD_TYPE_MILESTONE = 3;

    public static Intent newIntent(Context context, String namespace, String projectName, String commitSha) {
        Intent intent = new Intent(context, LoadSomeInfoActivity.class);
        intent.putExtra(EXTRA_PROJECT_NAMESPACE, namespace);
        intent.putExtra(EXTRA_PROJECT_NAME, projectName);
        intent.putExtra(EXTRA_COMMIT_SHA, commitSha);
        intent.putExtra(EXTRA_LOAD_TYPE, LOAD_TYPE_DIFF);
        return intent;
    }

    public static Intent newMergeRequestIntent(Context context, String namespace, String projectName, String mergeRequestId) {
        Intent intent = new Intent(context, LoadSomeInfoActivity.class);
        intent.putExtra(EXTRA_PROJECT_NAMESPACE, namespace);
        intent.putExtra(EXTRA_PROJECT_NAME, projectName);
        intent.putExtra(EXTRA_MERGE_REQUEST, mergeRequestId);
        intent.putExtra(EXTRA_LOAD_TYPE, LOAD_TYPE_MERGE_REQUEST);
        return intent;
    }

    public static Intent newBuildIntent(Context context, String namespace, String projectName, long buildId) {
        Intent intent = new Intent(context, LoadSomeInfoActivity.class);
        intent.putExtra(EXTRA_PROJECT_NAMESPACE, namespace);
        intent.putExtra(EXTRA_PROJECT_NAME, projectName);
        intent.putExtra(EXTRA_BUILD_ID, buildId);
        intent.putExtra(EXTRA_LOAD_TYPE, LOAD_TYPE_BUILD);
        return intent;
    }

    public static Intent newMilestoneIntent(Context context, String namespace, String projectName, String milestoneIid) {
        Intent intent = new Intent(context, LoadSomeInfoActivity.class);
        intent.putExtra(EXTRA_PROJECT_NAMESPACE, namespace);
        intent.putExtra(EXTRA_PROJECT_NAME, projectName);
        intent.putExtra(EXTRA_MILESTONE_ID, milestoneIid);
        intent.putExtra(EXTRA_LOAD_TYPE, LOAD_TYPE_MILESTONE);
        return intent;
    }

    @BindView(R.id.progress)
    View mProgress;

    private int mLoadType;

    private Project mProject;

    @OnClick(R.id.root)
    void onRootClicked() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        ButterKnife.bind(this);
        mProgress.setVisibility(View.VISIBLE);
        mLoadType = getIntent().getIntExtra(EXTRA_LOAD_TYPE, -1);
        Timber.d("Loading some info type: %d", mLoadType);

        switch (mLoadType) {
            case LOAD_TYPE_DIFF:
            case LOAD_TYPE_MERGE_REQUEST:
            case LOAD_TYPE_BUILD:
            case LOAD_TYPE_MILESTONE:
                String namespace = getIntent().getStringExtra(EXTRA_PROJECT_NAMESPACE);
                String project = getIntent().getStringExtra(EXTRA_PROJECT_NAME);
                App.get().getGitLab().getProject(namespace, project)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CustomSingleObserver<Project>() {

                            @Override
                            public void error(Throwable t) {
                                Timber.e(t);
                                LoadSomeInfoActivity.this.onError();
                            }

                            @Override
                            public void success(Project project) {
                                loadNextPart(project);
                            }
                        });
                break;
        }
    }

    private void loadNextPart(Project response) {
        mProject = response;
        switch (mLoadType) {
            case LOAD_TYPE_DIFF:
                String sha = getIntent().getStringExtra(EXTRA_COMMIT_SHA);
                App.get().getGitLab().getCommit(response.getId(), sha)
                        .compose(LoadSomeInfoActivity.this.<RepositoryCommit>bindToLifecycle())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CustomSingleObserver<RepositoryCommit>() {

                            @Override
                            public void error(Throwable t) {
                                Timber.e(t);
                                LoadSomeInfoActivity.this.onError();
                            }

                            @Override
                            public void success(RepositoryCommit repositoryCommit) {
                                Navigator.navigateToDiffActivity(LoadSomeInfoActivity.this, mProject, repositoryCommit);
                                finish();
                            }
                        });
                return;
            case LOAD_TYPE_MERGE_REQUEST:
                String mergeRequestId = getIntent().getStringExtra(EXTRA_MERGE_REQUEST);
                App.get().getGitLab().getMergeRequestsByIid(response.getId(), mergeRequestId)
                        .compose(LoadSomeInfoActivity.this.<List<MergeRequest>>bindToLifecycle())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CustomSingleObserver<List<MergeRequest>>() {

                            @Override
                            public void error(Throwable t) {
                                Timber.e(t);
                                LoadSomeInfoActivity.this.onError();
                            }

                            @Override
                            public void success(List<MergeRequest> mergeRequests) {
                                if (!mergeRequests.isEmpty()) {
                                    Navigator.navigateToMergeRequest(LoadSomeInfoActivity.this, mProject, mergeRequests.get(0));
                                    finish();
                                } else {
                                    LoadSomeInfoActivity.this.onError();
                                }
                            }
                        });
                return;
            case LOAD_TYPE_BUILD:
                long buildId = getIntent().getLongExtra(EXTRA_BUILD_ID, -1);
                App.get().getGitLab().getBuild(response.getId(), buildId)
                        .compose(LoadSomeInfoActivity.this.<Build>bindToLifecycle())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CustomSingleObserver<Build>() {

                            @Override
                            public void error(Throwable t) {
                                Timber.e(t);
                                LoadSomeInfoActivity.this.onError();
                            }

                            @Override
                            public void success(Build build) {
                                Navigator.navigateToBuild(LoadSomeInfoActivity.this, mProject, build);
                                finish();
                            }
                        });
                return;
            case LOAD_TYPE_MILESTONE:
                String milestoneId = getIntent().getStringExtra(EXTRA_MILESTONE_ID);
                App.get().getGitLab().getMilestonesByIid(response.getId(), milestoneId)
                        .compose(LoadSomeInfoActivity.this.<List<Milestone>>bindToLifecycle())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CustomSingleObserver<List<Milestone>>() {

                            @Override
                            public void error(Throwable t) {
                                Timber.e(t);
                                LoadSomeInfoActivity.this.onError();
                            }

                            @Override
                            public void success(List<Milestone> milestones) {
                                if (!milestones.isEmpty()) {
                                    Navigator.navigateToMilestone(LoadSomeInfoActivity.this, mProject, milestones.get(0));
                                    finish();
                                } else {
                                    LoadSomeInfoActivity.this.onError();
                                }
                            }
                        });
                return;
        }

    }

    private void onError() {
        Toast.makeText(LoadSomeInfoActivity.this, R.string.failed_to_load, Toast.LENGTH_SHORT)
                .show();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.do_nothing, R.anim.fade_out);
    }
}
