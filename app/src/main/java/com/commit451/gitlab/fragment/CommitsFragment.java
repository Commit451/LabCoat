package com.commit451.gitlab.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.DiffActivity;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.CommitsAdapter;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class CommitsFragment extends BaseFragment {

    public static CommitsFragment newInstance() {
        return new CommitsFragment();
    }

    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mCommitsListView;
    @Bind(R.id.message_text) TextView mMessageView;

    private Project mProject;
    private String mBranchName;
    private EventReceiver mEventReceiver;
    private LinearLayoutManager mCommitsLayoutManager;
    private CommitsAdapter mCommitsAdapter;
    private int mPage = -1;
    private boolean mLoading = false;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mCommitsLayoutManager.getChildCount();
            int totalItemCount = mCommitsLayoutManager.getItemCount();
            int firstVisibleItem = mCommitsLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mPage >= 0) {
                loadMore();
            }
        }
    };

    private final Callback<List<RepositoryCommit>> mCommitsCallback = new Callback<List<RepositoryCommit>>() {
        @Override
        public void onResponse(Response<List<RepositoryCommit>> response, Retrofit retrofit) {
            mLoading = false;
            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);

            if (!response.isSuccess()) {
                Timber.e("Commits response was not a success: %d", response.code());
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.connection_error_commits);
                mCommitsAdapter.setData(null);
                mPage = 0;
                return;
            }

            if (!response.body().isEmpty()) {
                mMessageView.setVisibility(View.GONE);
            } else if (mPage <= 0) {
                Timber.d("No commits have been made");
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_commits_found);
            }

            mCommitsAdapter.setData(response.body());

            if (response.body().isEmpty()) {
                mPage = -1;
            }
        }

        @Override
        public void onFailure(Throwable t) {
            mLoading = false;
            Timber.e(t, null);

            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);

            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.connection_error);
            mCommitsAdapter.setData(null);
            mPage = -1;
        }
    };

    private final Callback<List<RepositoryCommit>> mMoreCommitsCallback = new Callback<List<RepositoryCommit>>() {
        @Override
        public void onResponse(Response<List<RepositoryCommit>> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                return;
            }
            mLoading = false;
            mCommitsAdapter.setLoading(false);
            mPage++;
            mCommitsAdapter.addData(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mCommitsAdapter.setLoading(false);
            mLoading = false;
        }
    };

    private final CommitsAdapter.Listener mCommitsAdapterListener = new CommitsAdapter.Listener() {
        @Override
        public void onCommitClicked(RepositoryCommit commit) {
            getActivity().startActivity(DiffActivity.newInstance(getActivity(), mProject, commit));
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_commits, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mEventReceiver = new EventReceiver();
        LabCoatApp.bus().register(mEventReceiver);

        mCommitsAdapter = new CommitsAdapter(mCommitsAdapterListener);
        mCommitsLayoutManager = new LinearLayoutManager(getActivity());
        mCommitsListView.setLayoutManager(mCommitsLayoutManager);
        mCommitsListView.addItemDecoration(new DividerItemDecoration(getActivity()));
        mCommitsListView.setAdapter(mCommitsAdapter);
        mCommitsListView.addOnScrollListener(mOnScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        if (getActivity() instanceof ProjectActivity) {
            mProject = ((ProjectActivity) getActivity()).getProject();
            mBranchName = ((ProjectActivity) getActivity()).getBranchName();
            loadData();
        } else {
            throw new IllegalStateException("Incorrect parent activity");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        LabCoatApp.bus().unregister(mEventReceiver);
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }

        if (mProject == null || TextUtils.isEmpty(mBranchName)) {
            mSwipeRefreshLayout.setRefreshing(false);
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

        mPage = 0;
        mLoading = true;

        GitLabClient.instance().getCommits(mProject.getId(), mBranchName, mPage).enqueue(mCommitsCallback);
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

        if (mProject == null || TextUtils.isEmpty(mBranchName) || mPage < 0) {
            return;
        }

        mPage++;
        mLoading = true;
        mCommitsAdapter.setLoading(true);

        Timber.d("loadMore called for " + mPage);
        GitLabClient.instance().getCommits(mProject.getId(), mBranchName, mPage).enqueue(mMoreCommitsCallback);
    }

    private class EventReceiver {
        @Subscribe
        public void onProjectReload(ProjectReloadEvent event) {
            mProject = event.mProject;
            mBranchName = event.mBranchName;
            loadData();
        }
    }
}