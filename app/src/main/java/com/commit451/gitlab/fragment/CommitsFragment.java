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

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.CommitsAdapter;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.navigation.Navigator;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class CommitsFragment extends ButterKnifeFragment {

    public static CommitsFragment newInstance() {
        return new CommitsFragment();
    }

    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list) RecyclerView mCommitsListView;
    @BindView(R.id.message_text) TextView mMessageView;

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

    private final CommitsAdapter.Listener mCommitsAdapterListener = new CommitsAdapter.Listener() {
        @Override
        public void onCommitClicked(RepositoryCommit commit) {
            Navigator.navigateToDiffActivity(getActivity(), mProject, commit);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_commits, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEventReceiver = new EventReceiver();
        App.bus().register(mEventReceiver);

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
            mBranchName = ((ProjectActivity) getActivity()).getRef();
            loadData();
        } else {
            throw new IllegalStateException("Incorrect parent activity");
        }
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

        App.get().getGitLab().getCommits(mProject.getId(), mBranchName, mPage)
                .compose(this.<List<RepositoryCommit>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<RepositoryCommit>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        mLoading = false;
                        Timber.e(e);
                        mSwipeRefreshLayout.setRefreshing(false);
                        mMessageView.setVisibility(View.VISIBLE);
                        mMessageView.setText(R.string.connection_error_commits);
                        mCommitsAdapter.setData(null);
                        mPage = -1;
                    }

                    @Override
                    public void onNext(List<RepositoryCommit> repositoryCommits) {
                        mLoading = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (!repositoryCommits.isEmpty()) {
                            mMessageView.setVisibility(View.GONE);
                        } else {
                            mMessageView.setVisibility(View.VISIBLE);
                            mMessageView.setText(R.string.no_commits_found);
                        }
                        mCommitsAdapter.setData(repositoryCommits);
                        if (repositoryCommits.isEmpty()) {
                            mPage = -1;
                        }
                    }
                });
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

        Timber.d("loadMore called for %s", mPage);
        App.get().getGitLab().getCommits(mProject.getId(), mBranchName, mPage)
                .compose(this.<List<RepositoryCommit>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<RepositoryCommit>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        mLoading = false;
                        Timber.e(e);
                        mCommitsAdapter.setLoading(false);
                    }

                    @Override
                    public void onNext(List<RepositoryCommit> repositoryCommits) {
                        mLoading = false;
                        mCommitsAdapter.setLoading(false);
                        if (repositoryCommits.isEmpty()) {
                            mPage = -1;
                            return;
                        }
                        mCommitsAdapter.addData(repositoryCommits);
                    }
                });
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