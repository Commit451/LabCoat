package com.commit451.gitlab.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.BuildArtifactsAdapter;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.BuildChangedEvent;
import com.commit451.gitlab.model.api.Artifact;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;
import com.squareup.otto.Subscribe;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * Shows the build artifacts
 */
public class BuildArtifactsFragment extends ButterKnifeFragment {

    private static final String KEY_PROJECT = "project";
    private static final String KEY_BUILD = "build";

    public static BuildArtifactsFragment newInstance(Project project, Build build) {
        BuildArtifactsFragment fragment = new BuildArtifactsFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_PROJECT, Parcels.wrap(project));
        args.putParcelable(KEY_BUILD, Parcels.wrap(build));
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list) RecyclerView mCommitsListView;
    @BindView(R.id.message_text) TextView mMessageView;

    private Project mProject;
    private Build mBuild;
    private BuildArtifactsAdapter mArtifactsAdapter;

    EventReceiver mEventReceiver;

    private final EasyCallback<List<Artifact>> mCommitsCallback = new EasyCallback<List<Artifact>>() {
        @Override
        public void success(@NonNull List<Artifact> response) {
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (!response.isEmpty()) {
                mMessageView.setVisibility(View.GONE);
            } else {
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_commits_found);
            }
            mArtifactsAdapter.setData(response);
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.no_build_artifacts_found);
            mArtifactsAdapter.setData(null);
        }
    };

    private final BuildArtifactsAdapter.Listener mAdapterListener = new BuildArtifactsAdapter.Listener() {
        @Override
        public void onFolderClicked(Artifact treeItem) {

        }

        @Override
        public void onFileClicked(Artifact treeItem) {

        }

        @Override
        public void onCopyClicked(Artifact treeItem) {

        }

        @Override
        public void onShareClicked(Artifact treeItem) {

        }

        @Override
        public void onOpenInBrowserClicked(Artifact treeItem) {

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
        return inflater.inflate(R.layout.fragment_build_artifacts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mArtifactsAdapter = new BuildArtifactsAdapter(mAdapterListener);
        mCommitsListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCommitsListView.addItemDecoration(new DividerItemDecoration(getActivity()));
        mCommitsListView.setAdapter(mArtifactsAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        loadData();
        mEventReceiver = new EventReceiver();
        App.bus().register(mEventReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.bus().unregister(mEventReceiver);
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        GitLabClient.instance().getBuildArtifacts(mProject.getId(), mBuild.getId()).enqueue(mCommitsCallback);
    }

    private class EventReceiver {

        @Subscribe
        public void onBuildChanged(BuildChangedEvent event) {
            if (mBuild.getId() == event.build.getId()) {
                mBuild = event.build;
                loadData();
            }
        }
    }
}