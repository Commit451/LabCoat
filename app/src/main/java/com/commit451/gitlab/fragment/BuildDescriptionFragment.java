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
import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.event.BuildChangedEvent;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.model.api.Runner;
import com.commit451.gitlab.util.DateUtil;
import com.squareup.otto.Subscribe;

import org.parceler.Parcels;

import java.util.Date;

import butterknife.BindView;
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
    ViewGroup mRoot;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.text_duration)
    TextView mTextDuration;
    @BindView(R.id.text_created)
    TextView mTextCreated;
    @BindView(R.id.text_finished)
    TextView mTextFinished;
    @BindView(R.id.text_runner)
    TextView mTextRunner;
    @BindView(R.id.text_author)
    TextView mTextAuthor;
    @BindView(R.id.text_message)
    TextView mTextMessage;

    Project mProject;
    Build mBuild;

    EventReceiver mEventReceiver;

    private final EasyCallback<Build> mLoadBuildCallback = new EasyCallback<Build>() {
        @Override
        public void success(@NonNull Build response) {
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mBuild = response;
            bindBuild(response);
            App.bus().post(new BuildChangedEvent(response));
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            if (getView() == null) {
                return;
            }
            Snackbar.make(mRoot, R.string.unable_to_load_build, Snackbar.LENGTH_LONG)
                    .show();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProject = Parcels.unwrap(getArguments().getParcelable(KEY_PROJECT));
        mBuild = Parcels.unwrap(getArguments().getParcelable(KEY_BUILD));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_build_description, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });
        bindBuild(mBuild);
        mEventReceiver = new EventReceiver();
        App.bus().register(mEventReceiver);
    }

    private void load() {
        App.instance().getGitLab().getBuild(mProject.getId(), mBuild.getId()).enqueue(mLoadBuildCallback);
    }

    private void bindBuild(Build build) {
        Date finishedTime = build.getFinishedAt();
        if (finishedTime == null) {
            finishedTime = new Date();
        }
        String timeTaken = DateUtil.getTimeTaken(build.getStartedAt(), finishedTime);
        String duration = String.format(getString(R.string.build_duration), timeTaken);
        mTextDuration.setText(duration);
        String created = String.format(getString(R.string.build_created), DateUtil.getRelativeTimeSpanString(getActivity(), build.getCreatedAt()));
        mTextCreated.setText(created);
        if (build.getFinishedAt() != null) {
            String finished = String.format(getString(R.string.build_finished), DateUtil.getRelativeTimeSpanString(getActivity(), build.getFinishedAt()));
            mTextFinished.setText(finished);
            mTextFinished.setVisibility(View.VISIBLE);
        } else {
            mTextFinished.setVisibility(View.GONE);
        }
        if (build.getRunner() != null) {
            bindRunner(build.getRunner());
        }
        if(build.getCommit() != null) {
            bindCommit(build.getCommit());
        }
    }

    private void bindRunner(Runner runner) {
        String runnerNum = String.format(getString(R.string.runner_number), String.valueOf(runner.getId()));
        mTextRunner.setText(runnerNum);
    }

    private void bindCommit(RepositoryCommit commit) {
        String authorText = String.format(getString(R.string.build_commit_author), commit.getAuthorName());
        mTextAuthor.setText(authorText);
        String messageText = String.format(getString(R.string.build_commit_message), commit.getMessage());
        mTextMessage.setText(messageText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.bus().unregister(mEventReceiver);
    }

    private class EventReceiver {

        @Subscribe
        public void onBuildChangedEvent(BuildChangedEvent event) {
            if (mBuild.getId() == event.build.getId()) {
                mBuild = event.build;
                bindBuild(mBuild);
            }
        }
    }

}
