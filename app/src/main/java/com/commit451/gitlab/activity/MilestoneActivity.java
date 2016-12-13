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
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.reptar.FocusedSingleObserver;
import com.commit451.reptar.retrofit.ResponseSingleObserver;

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
    View mRoot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView mIssuesRecyclerView;
    MilestoneIssuesAdapter mMilestoneIssuesAdapter;
    LinearLayoutManager mIssuesLayoutManager;
    @BindView(R.id.message_text)
    TextView mMessageText;
    @BindView(R.id.progress)
    View mProgress;

    MenuItem mOpenCloseMenuItem;

    Project mProject;
    Milestone mMilestone;
    Uri mNextPageUrl;
    boolean mLoading = false;

    EventReceiver mEventReceiver;

    @OnClick(R.id.add)
    void onAddClick(View fab) {
        Navigator.navigateToAddIssue(MilestoneActivity.this, fab, mProject);
    }

    @OnClick(R.id.edit)
    void onEditClicked(View fab) {
        Navigator.navigateToEditMilestone(MilestoneActivity.this, fab, mProject, mMilestone);
    }

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mIssuesLayoutManager.getChildCount();
            int totalItemCount = mIssuesLayoutManager.getItemCount();
            int firstVisibleItem = mIssuesLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMore();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_milestone);
        ButterKnife.bind(this);
        mEventReceiver = new EventReceiver();
        App.bus().register(mEventReceiver);

        mProject = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));
        mMilestone = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_MILESTONE));

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.inflateMenu(R.menu.menu_milestone);
        mOpenCloseMenuItem = mToolbar.getMenu().findItem(R.id.action_close);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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

        mMilestoneIssuesAdapter = new MilestoneIssuesAdapter(new MilestoneIssuesAdapter.Listener() {
            @Override
            public void onIssueClicked(Issue issue) {
                Navigator.navigateToIssue(MilestoneActivity.this, mProject, issue);
            }
        });
        bind(mMilestone);
        mIssuesRecyclerView.setAdapter(mMilestoneIssuesAdapter);
        mIssuesLayoutManager = new LinearLayoutManager(this);
        mIssuesRecyclerView.setLayoutManager(mIssuesLayoutManager);
        mIssuesRecyclerView.addItemDecoration(new DividerItemDecoration(this));
        mIssuesRecyclerView.addOnScrollListener(mOnScrollListener);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        App.bus().unregister(mEventReceiver);
    }

    private void bind(Milestone milestone) {
        mToolbar.setTitle(milestone.getTitle());
        mMilestoneIssuesAdapter.setMilestone(milestone);
        setOpenCloseMenuStatus();
    }

    private void loadData() {
        mMessageText.setVisibility(View.GONE);
        mLoading = true;
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        App.get().getGitLab().getMilestoneIssues(mProject.getId(), mMilestone.getId())
                .compose(this.<Response<List<Issue>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseSingleObserver<List<Issue>>() {

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mLoading = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                        mMessageText.setVisibility(View.VISIBLE);
                        mMessageText.setText(R.string.connection_error_issues);
                        mMilestoneIssuesAdapter.setIssues(null);
                    }

                    @Override
                    protected void onResponseSuccess(List<Issue> issues) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mLoading = false;

                        if (!issues.isEmpty()) {
                            mMessageText.setVisibility(View.GONE);
                        } else {
                            Timber.d("No issues found");
                            mMessageText.setVisibility(View.VISIBLE);
                            mMessageText.setText(R.string.no_issues);
                        }

                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        mMilestoneIssuesAdapter.setIssues(issues);
                    }
                });
    }

    private void loadMore() {

        if (mNextPageUrl == null) {
            return;
        }

        mLoading = true;

        Timber.d("loadMore called for %s", mNextPageUrl);
        App.get().getGitLab().getMilestoneIssues(mNextPageUrl.toString())
                .compose(this.<Response<List<Issue>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseSingleObserver<List<Issue>>() {

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mLoading = false;
                    }

                    @Override
                    protected void onResponseSuccess(List<Issue> issues) {
                        mLoading = false;
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        mMilestoneIssuesAdapter.addIssues(issues);
                    }
                });
    }

    private void closeOrOpenIssue() {
        mProgress.setVisibility(View.VISIBLE);
        if (mMilestone.getState().equals(Milestone.STATE_ACTIVE)) {
            updateMilestoneStatus(App.get().getGitLab().updateMilestoneStatus(mProject.getId(), mMilestone.getId(), Milestone.STATE_EVENT_CLOSE));
        } else {
            updateMilestoneStatus(App.get().getGitLab().updateMilestoneStatus(mProject.getId(), mMilestone.getId(), Milestone.STATE_EVENT_ACTIVATE));
        }
    }

    private void updateMilestoneStatus(Single<Milestone> observable) {
        observable.compose(this.<Milestone>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FocusedSingleObserver<Milestone>() {

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mProgress.setVisibility(View.GONE);
                        Snackbar.make(mRoot, getString(R.string.failed_to_create_milestone), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(Milestone milestone) {
                        mProgress.setVisibility(View.GONE);
                        mMilestone = milestone;
                        App.bus().post(new MilestoneChangedEvent(mMilestone));
                        setOpenCloseMenuStatus();
                    }
                });
    }

    private void setOpenCloseMenuStatus() {
        mOpenCloseMenuItem.setTitle(mMilestone.getState().equals(Milestone.STATE_CLOSED) ? R.string.reopen : R.string.close);
    }

    private class EventReceiver {

        @Subscribe
        public void onMilestoneChanged(MilestoneChangedEvent event) {
            if (mMilestone.getId() == event.mMilestone.getId()) {
                mMilestone = event.mMilestone;
                bind(mMilestone);
            }
        }
    }
}
