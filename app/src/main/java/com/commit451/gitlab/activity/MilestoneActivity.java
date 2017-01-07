package com.commit451.gitlab.activity;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.adapter.MilestoneIssuesAdapter;
import com.commit451.gitlab.event.MilestoneChangedEvent;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.util.LinkHeaderParser;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

public class MilestoneActivity extends BaseActivity {

    private static final String EXTRA_PROJECT = "extra_project";
    private static final String EXTRA_MILESTONE = "extra_milestone";

    public static Intent newIntent(Context context, Project project, Milestone milestone) {
        Intent intent = new Intent(context, MilestoneActivity.class);
        intent.putExtra(EXTRA_PROJECT, Parcels.wrap(project));
        intent.putExtra(EXTRA_MILESTONE, Parcels.wrap(milestone));
        return intent;
    }

    @BindView(R.id.root)
    View root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listIssues;
    @BindView(R.id.message_text)
    TextView textMessage;
    @BindView(R.id.progress)
    View progress;

    MilestoneIssuesAdapter adapterMilestoneIssues;
    LinearLayoutManager layoutManagerIssues;
    MenuItem menuItemOpenClose;

    Project project;
    Milestone milestone;
    Uri nextPageUrl;
    boolean loading = false;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManagerIssues.getChildCount();
            int totalItemCount = layoutManagerIssues.getItemCount();
            int firstVisibleItem = layoutManagerIssues.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore();
            }
        }
    };

    @OnClick(R.id.add)
    void onAddClick(View fab) {
        Navigator.navigateToAddIssue(MilestoneActivity.this, fab, project);
    }

    @OnClick(R.id.edit)
    void onEditClicked(View fab) {
        Navigator.navigateToEditMilestone(MilestoneActivity.this, fab, project, milestone);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_milestone);
        ButterKnife.bind(this);
        App.bus().register(this);

        project = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));
        milestone = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_MILESTONE));

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.menu_milestone);
        menuItemOpenClose = toolbar.getMenu().findItem(R.id.action_close);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_close:
                        closeOrOpenIssue();
                        return true;
                }
                return false;
            }
        });

        adapterMilestoneIssues = new MilestoneIssuesAdapter(new MilestoneIssuesAdapter.Listener() {
            @Override
            public void onIssueClicked(Issue issue) {
                Navigator.navigateToIssue(MilestoneActivity.this, project, issue);
            }
        });
        bind(milestone);
        listIssues.setAdapter(adapterMilestoneIssues);
        layoutManagerIssues = new LinearLayoutManager(this);
        listIssues.setLayoutManager(layoutManagerIssues);
        listIssues.addItemDecoration(new DividerItemDecoration(this));
        listIssues.addOnScrollListener(onScrollListener);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.bus().unregister(this);
    }

    private void bind(Milestone milestone) {
        toolbar.setTitle(milestone.getTitle());
        adapterMilestoneIssues.setMilestone(milestone);
        setOpenCloseMenuStatus();
    }

    private void loadData() {
        textMessage.setVisibility(View.GONE);
        loading = true;
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        App.get().getGitLab().getMilestoneIssues(project.getId(), milestone.getId())
                .compose(this.<Response<List<Issue>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Issue>>() {

                    @Override
                    public void error(Throwable t) {
                        Timber.e(t);
                        loading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.connection_error_issues);
                        adapterMilestoneIssues.setIssues(null);
                    }

                    @Override
                    public void responseSuccess(List<Issue> issues) {
                        swipeRefreshLayout.setRefreshing(false);
                        loading = false;

                        if (!issues.isEmpty()) {
                            textMessage.setVisibility(View.GONE);
                        } else {
                            Timber.d("No issues found");
                            textMessage.setVisibility(View.VISIBLE);
                            textMessage.setText(R.string.no_issues);
                        }

                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        adapterMilestoneIssues.setIssues(issues);
                    }
                });
    }

    private void loadMore() {

        if (nextPageUrl == null) {
            return;
        }

        loading = true;

        Timber.d("loadMore called for %s", nextPageUrl);
        App.get().getGitLab().getMilestoneIssues(nextPageUrl.toString())
                .compose(this.<Response<List<Issue>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Issue>>() {

                    @Override
                    public void error(Throwable e) {
                        Timber.e(e);
                        loading = false;
                    }

                    @Override
                    public void responseSuccess(List<Issue> issues) {
                        loading = false;
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        adapterMilestoneIssues.addIssues(issues);
                    }
                });
    }

    private void closeOrOpenIssue() {
        progress.setVisibility(View.VISIBLE);
        if (milestone.getState().equals(Milestone.STATE_ACTIVE)) {
            updateMilestoneStatus(App.get().getGitLab().updateMilestoneStatus(project.getId(), milestone.getId(), Milestone.STATE_EVENT_CLOSE));
        } else {
            updateMilestoneStatus(App.get().getGitLab().updateMilestoneStatus(project.getId(), milestone.getId(), Milestone.STATE_EVENT_ACTIVATE));
        }
    }

    private void updateMilestoneStatus(Single<Milestone> observable) {
        observable.compose(this.<Milestone>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Milestone>() {

                    @Override
                    public void error(Throwable e) {
                        Timber.e(e);
                        progress.setVisibility(View.GONE);
                        Snackbar.make(root, getString(R.string.failed_to_create_milestone), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void success(Milestone milestone) {
                        progress.setVisibility(View.GONE);
                        MilestoneActivity.this.milestone = milestone;
                        App.bus().post(new MilestoneChangedEvent(MilestoneActivity.this.milestone));
                        setOpenCloseMenuStatus();
                    }
                });
    }

    private void setOpenCloseMenuStatus() {
        menuItemOpenClose.setTitle(milestone.getState().equals(Milestone.STATE_CLOSED) ? R.string.reopen : R.string.close);
    }

    @Subscribe
    public void onMilestoneChanged(MilestoneChangedEvent event) {
        if (milestone.getId() == event.mMilestone.getId()) {
            milestone = event.mMilestone;
            bind(milestone);
        }
    }
}
