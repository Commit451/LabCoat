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
import com.commit451.gitlab.adapter.CommitAdapter;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.event.MergeRequestChangedEvent;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomSingleObserver;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
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

    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listCommits;
    @BindView(R.id.message_text)
    TextView textMessage;

    private LinearLayoutManager layoutManagerCommits;
    private CommitAdapter adapterCommits;

    private Project project;
    private MergeRequest mergeRequest;
    private int page = -1;
    private boolean loading = false;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManagerCommits.getChildCount();
            int totalItemCount = layoutManagerCommits.getItemCount();
            int firstVisibleItem = layoutManagerCommits.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && page >= 0) {
                loadMore();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        project = Parcels.unwrap(getArguments().getParcelable(KEY_PROJECT));
        mergeRequest = Parcels.unwrap(getArguments().getParcelable(KEY_MERGE_REQUEST));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merge_request_commits, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapterCommits = new CommitAdapter(new CommitAdapter.Listener() {
            @Override
            public void onCommitClicked(RepositoryCommit commit) {
                Navigator.navigateToDiffActivity(getActivity(), project, commit);
            }
        });
        layoutManagerCommits = new LinearLayoutManager(getActivity());
        listCommits.setLayoutManager(layoutManagerCommits);
        listCommits.addItemDecoration(new DividerItemDecoration(getActivity()));
        listCommits.setAdapter(adapterCommits);
        listCommits.addOnScrollListener(onScrollListener);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        loadData();
        App.bus().register(this);
    }

    @Override
    public void onDestroyView() {
        App.bus().unregister(this);
        super.onDestroyView();
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        page = 0;
        loading = true;

        App.get().getGitLab().getMergeRequestCommits(project.getId(), mergeRequest.getId())
                .compose(this.<List<RepositoryCommit>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<List<RepositoryCommit>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        loading = false;
                        Timber.e(e);
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.connection_error_commits);
                        adapterCommits.setData(null);
                        page = -1;
                    }

                    @Override
                    public void success(@NonNull List<RepositoryCommit> repositoryCommits) {
                        loading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        if (!repositoryCommits.isEmpty()) {
                            textMessage.setVisibility(View.GONE);
                        } else {
                            textMessage.setVisibility(View.VISIBLE);
                            textMessage.setText(R.string.no_commits_found);
                        }
                        adapterCommits.setData(repositoryCommits);
                        if (repositoryCommits.isEmpty()) {
                            page = -1;
                        }
                    }
                });
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

        page++;
        loading = true;
        //adapterCommits.setLoading(true);

        Timber.d("loadMore called for %s", page);
        //TODO is this even a thing?
    }

    @Subscribe
    public void onMergeRequestChangedEvent(MergeRequestChangedEvent event) {
        if (mergeRequest.getId() == event.mergeRequest.getId()) {
            mergeRequest = event.mergeRequest;
            loadData();
        }
    }

}