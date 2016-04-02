package com.commit451.gitlab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.event.BuildChangedEvent;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.model.api.Runner;
import com.commit451.gitlab.util.DateUtils;
import com.squareup.otto.Subscribe;

import org.parceler.Parcels;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows the details of a build
 */
public class BuildDescriptionFragment extends BaseFragment {

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

    @Bind(R.id.root)
    ViewGroup mRoot;
    @Bind(R.id.text_duration)
    TextView mTextDuration;
    @Bind(R.id.text_created)
    TextView mTextCreated;
    @Bind(R.id.text_finished)
    TextView mTextFinished;
    @Bind(R.id.text_runner)
    TextView mTextRunner;
    @Bind(R.id.text_author)
    TextView mTextAuthor;
    @Bind(R.id.text_message)
    TextView mTextMessage;

    Project mProject;
    Build mBuild;

    EventReceiver mEventReceiver;

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
        ButterKnife.bind(this, view);

        bindBuild(mBuild);
        mEventReceiver = new EventReceiver();
        LabCoatApp.bus().register(mEventReceiver);
    }

    private void bindBuild(Build build) {
        Date finishedTime = build.getFinishedAt();
        if (finishedTime == null) {
            finishedTime = new Date();
        }
        String timeTaken = DateUtils.getTimeTaken(build.getStartedAt(), finishedTime);
        String duration = String.format(getString(R.string.build_duration), timeTaken);
        mTextDuration.setText(duration);
        String created = String.format(getString(R.string.build_created), DateUtils.getRelativeTimeSpanString(getActivity(), build.getCreatedAt()));
        mTextCreated.setText(created);
        String finished = String.format(getString(R.string.build_finished), DateUtils.getRelativeTimeSpanString(getActivity(), build.getFinishedAt()));
        mTextFinished.setText(finished);
        bindRunner(build.getRunner());
        bindCommit(build.getCommit());
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
        ButterKnife.unbind(this);
        LabCoatApp.bus().unregister(mEventReceiver);
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
