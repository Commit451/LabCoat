package com.commit451.gitlab.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.event.BuildChangedEvent;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.model.api.Runner;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.util.DateUtil;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.Date;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Shows the details of a build
 */
public class BuildDescriptionFragment extends ButterKnifeFragment {

    private static final String KEY_PROJECT = "project";
    private static final String KEY_BUILD = "build";

    public static BuildDescriptionFragment newInstance(Project project, Build build) {
        BuildDescriptionFragment fragment = new BuildDescriptionFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_PROJECT, Parcels.wrap(project));
        args.putParcelable(KEY_BUILD, Parcels.wrap(build));
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.root)
    ViewGroup root;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.text_status)
    TextView textStatus;
    @BindView(R.id.text_duration)
    TextView textDuration;
    @BindView(R.id.text_created)
    TextView textCreated;
    @BindView(R.id.text_finished)
    TextView textFinished;
    @BindView(R.id.text_runner)
    TextView textRunner;
    @BindView(R.id.text_ref)
    TextView textRef;
    @BindView(R.id.text_author)
    TextView textAuthor;
    @BindView(R.id.text_message)
    TextView textMessage;

    Project project;
    Build build;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        project = Parcels.unwrap(getArguments().getParcelable(KEY_PROJECT));
        build = Parcels.unwrap(getArguments().getParcelable(KEY_BUILD));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_build_description, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });
        bindBuild(build);
        App.bus().register(this);
    }

    private void load() {
        App.get().getGitLab().getBuild(project.getId(), build.getId())
                .compose(this.<Build>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Build>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        Snackbar.make(root, R.string.unable_to_load_build, Snackbar.LENGTH_LONG)
                                .show();
                    }

                    @Override
                    public void success(@NonNull Build build) {
                        swipeRefreshLayout.setRefreshing(false);
                        BuildDescriptionFragment.this.build = build;
                        bindBuild(build);
                        App.bus().post(new BuildChangedEvent(build));
                    }
                });
    }

    private void bindBuild(Build build) {
        Date finishedTime = build.getFinishedAt();
        if (finishedTime == null) {
            finishedTime = new Date();
        }
        Date startedTime = build.getStartedAt();
        if (startedTime == null) {
            startedTime = new Date();
        }
        String status = String.format(getString(R.string.build_status), build.getStatus());
        textStatus.setText(status);
        String timeTaken = DateUtil.getTimeTaken(startedTime, finishedTime);
        String duration = String.format(getString(R.string.build_duration), timeTaken);
        textDuration.setText(duration);
        String created = String.format(getString(R.string.build_created), DateUtil.getRelativeTimeSpanString(getActivity(), build.getCreatedAt()));
        textCreated.setText(created);
        String ref = String.format(getString(R.string.build_ref), build.getRef());
        textRef.setText(ref);
        if (build.getFinishedAt() != null) {
            String finished = String.format(getString(R.string.build_finished), DateUtil.getRelativeTimeSpanString(getActivity(), build.getFinishedAt()));
            textFinished.setText(finished);
            textFinished.setVisibility(View.VISIBLE);
        } else {
            textFinished.setVisibility(View.GONE);
        }
        if (build.getRunner() != null) {
            bindRunner(build.getRunner());
        }
        if (build.getCommit() != null) {
            bindCommit(build.getCommit());
        }
    }

    private void bindRunner(Runner runner) {
        String runnerNum = String.format(getString(R.string.runner_number), String.valueOf(runner.getId()));
        textRunner.setText(runnerNum);
    }

    private void bindCommit(RepositoryCommit commit) {
        String authorText = String.format(getString(R.string.build_commit_author), commit.getAuthorName());
        textAuthor.setText(authorText);
        String messageText = String.format(getString(R.string.build_commit_message), commit.getMessage());
        textMessage.setText(messageText);
    }

    @Override
    public void onDestroyView() {
        App.bus().unregister(this);
        super.onDestroyView();
    }


    @Subscribe
    public void onBuildChangedEvent(BuildChangedEvent event) {
        if (build.getId() == event.build.getId()) {
            build = event.build;
            bindBuild(build);
        }
    }

}
