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

import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.CommitsAdapter;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.event.MergeRequestChangedEvent;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.navigation.Navigator;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * Like {@link CommitsFragment} but showing commits for a merge request
 */
public class MergeRequestCommitsFragment extends ButterKnifeFragment {

    private static final String KEY_PROJECT = "project";
    private static final String KEY_MERGE_REQUEST = "merge_request";

    public static MergeRequestCommitsFragment newInstance(Project project, MergeRequest mergeRequest) {
        MergeRequestCommitsFragment fragment = new MergeRequestCommitsFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_PROJECT, Parcels.wrap(project));
        args.putParcelable(KEY_MERGE_REQUEST, Parcels.wrap(mergeRequest));
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list) RecyclerView mCommitsListView;
    @BindView(R.id.message_text) TextView mMessageView;

    private Project mProject;
    private MergeRequest mMergeRequest;
    private LinearLayoutManager mCommitsLayoutManager;
    private CommitsAdapter mCommitsAdapter;
    private int mPage = -1;
    private boolean mLoading = false;

    EventReceiver mEventReceiver;

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

    private final EasyCallback<List<RepositoryCommit>> mCommitsCallback = new EasyCallback<List<RepositoryCommit>>() {
        @Override
        public void success(@NonNull List<RepositoryCommit> response) {
            mLoading = false;
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
            mCommitsAdapter.setData(response);
            if (response.isEmpty()) {
                mPage = -1;
            }
        }

        @Override
        public void failure(Throwable t) {
            mLoading = false;
            Timber.e(t);
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.connection_error_commits);
            mCommitsAdapter.setData(null);
            mPage = -1;
        }
    };

    private final EasyCallback<List<RepositoryCommit>> mMoreCommitsCallback = new EasyCallback<List<RepositoryCommit>>() {
        @Override
        public void success(@NonNull List<RepositoryCommit> response) {
            mLoading = false;
            mCommitsAdapter.setLoading(false);
            if (response.isEmpty()) {
                mPage = -1;
                return;
            }
            mCommitsAdapter.addData(response);
        }

        @Override
        public void failure(Throwable t) {
            mLoading = false;
            Timber.e(t);
            mCommitsAdapter.setLoading(false);
        }
    };

    private final CommitsAdapter.Listener mCommitsAdapterListener = new CommitsAdapter.Listener() {
        @Override
        public void onCommitClicked(RepositoryCommit commit) {
            Navigator.navigateToDiffActivity(getActivity(), mProject, commit);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProject = Parcels.unwrap(getArguments().getParcelable(KEY_PROJECT));
        mMergeRequest = Parcels.unwrap(getArguments().getParcelable(KEY_MERGE_REQUEST));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merge_request_commits, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        mPage = 0;
        mLoading = true;

        App.instance().getGitLab().getMergeRequestCommits(mProject.getId(), mMergeRequest.getId()).enqueue(mCommitsCallback);
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

        mPage++;
        mLoading = true;
        //mCommitsAdapter.setLoading(true);

        Timber.d("loadMore called for %s", mPage);
        //TODO is this even a thing?
    }

    private class EventReceiver {

        @Subscribe
        public void onMergeRequestChangedEvent(MergeRequestChangedEvent event) {
            if (mMergeRequest.getId() == event.mergeRequest.getId()) {
                mMergeRequest = event.mergeRequest;
                loadData();
            }
        }
    }
}