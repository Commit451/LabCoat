package com.commit451.gitlab.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.commit451.gitlab.adapter.CommitAdapter;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomSingleObserver;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class CommitsFragment extends ButterKnifeFragment {

    public static CommitsFragment newInstance() {
        return new CommitsFragment();
    }

    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listCommits;
    @BindView(R.id.message_text)
    TextView textMessage;

    LinearLayoutManager layoutManagerCommits;
    CommitAdapter adapterCommits;

    Project project;
    String branchName;
    int page = -1;
    boolean loading;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_commits, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App.bus().register(this);

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

        if (getActivity() instanceof ProjectActivity) {
            project = ((ProjectActivity) getActivity()).getProject();
            branchName = ((ProjectActivity) getActivity()).getRef();
            loadData();
        } else {
            throw new IllegalStateException("Incorrect parent activity");
        }
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

        if (project == null || TextUtils.isEmpty(branchName)) {
            swipeRefreshLayout.setRefreshing(false);
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

        App.get().getGitLab().getCommits(project.getId(), branchName, page)
                .compose(this.<List<RepositoryCommit>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<List<RepositoryCommit>>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        loading = false;
                        Timber.e(t);
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

        if (project == null || TextUtils.isEmpty(branchName) || page < 0) {
            return;
        }

        page++;
        loading = true;
        adapterCommits.setLoading(true);

        Timber.d("loadMore called for %s", page);
        App.get().getGitLab().getCommits(project.getId(), branchName, page)
                .compose(this.<List<RepositoryCommit>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<List<RepositoryCommit>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        loading = false;
                        Timber.e(e);
                        adapterCommits.setLoading(false);
                    }

                    @Override
                    public void success(@NonNull List<RepositoryCommit> repositoryCommits) {
                        loading = false;
                        adapterCommits.setLoading(false);
                        if (repositoryCommits.isEmpty()) {
                            page = -1;
                            return;
                        }
                        adapterCommits.addData(repositoryCommits);
                    }
                });
    }

    @Subscribe
    public void onProjectReload(ProjectReloadEvent event) {
        project = event.project;
        branchName = event.branchName;
        loadData();
    }
}