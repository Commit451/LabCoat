package com.commit451.gitlab.activity;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.MilestoneIssuesAdapter;
import com.commit451.gitlab.api.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.MilestoneChangedEvent;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.NavigationManager;
import com.commit451.gitlab.util.PaginationUtil;
import com.squareup.otto.Subscribe;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import timber.log.Timber;

public class MilestoneActivity extends BaseActivity {

    private static final String EXTRA_PROJECT = "extra_project";
    private static final String EXTRA_MILESTONE = "extra_milestone";

    public static Intent newInstance(Context context, Project project, Milestone milestone) {
        Intent intent = new Intent(context, MilestoneActivity.class);
        intent.putExtra(EXTRA_PROJECT, Parcels.wrap(project));
        intent.putExtra(EXTRA_MILESTONE, Parcels.wrap(milestone));
        return intent;
    }

    @Bind(R.id.root)
    View mRoot;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list)
    RecyclerView mIssuesRecyclerView;
    MilestoneIssuesAdapter mMilestoneIssuesAdapter;
    LinearLayoutManager mIssuesLayoutManager;
    @Bind(R.id.message_text)
    TextView mMessageText;

    private Project mProject;
    private Milestone mMilestone;
    private Uri mNextPageUrl;
    private boolean mLoading = false;

    EventReceiver mEventReceiver;

    @OnClick(R.id.add)
    void onAddClick() {
        NavigationManager.navigateToAddIssue(MilestoneActivity.this, null, mProject);
    }

    @OnClick(R.id.edit)
    void onEditClicked(View fab) {
        NavigationManager.navigateToEditMilestone(MilestoneActivity.this, fab, mProject, mMilestone);
    }

    private final Callback<List<Issue>> mIssuesCallback = new EasyCallback<List<Issue>>() {
        @Override
        public void onResponse(@NonNull List<Issue> response) {
            mSwipeRefreshLayout.setRefreshing(false);
            mLoading = false;

            if (!response.isEmpty()) {
                mMessageText.setVisibility(View.GONE);
            } else {
                Timber.d("No issues found");
                mMessageText.setVisibility(View.VISIBLE);
                mMessageText.setText(R.string.no_issues);
            }

            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
            mMilestoneIssuesAdapter.setIssues(response);
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            mLoading = false;
            mSwipeRefreshLayout.setRefreshing(false);
            mMessageText.setVisibility(View.VISIBLE);
            mMessageText.setText(R.string.connection_error_issues);
            mMilestoneIssuesAdapter.setIssues(null);
        }
    };

    private final Callback<List<Issue>> mMoreIssuesCallback = new EasyCallback<List<Issue>>() {
        @Override
        public void onResponse(@NonNull List<Issue> response) {
            mLoading = false;
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
            mMilestoneIssuesAdapter.addIssues(response);
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            mLoading = false;
        }
    };

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
        LabCoatApp.bus().register(mEventReceiver);

        mProject = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));
        mMilestone = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_MILESTONE));

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mMilestoneIssuesAdapter = new MilestoneIssuesAdapter(new MilestoneIssuesAdapter.Listener() {
            @Override
            public void onIssueClicked(Issue issue) {
                NavigationManager.navigateToIssue(MilestoneActivity.this, mProject, issue);
            }
        });
        bind(mMilestone);
        mIssuesRecyclerView.setAdapter(mMilestoneIssuesAdapter);
        mIssuesLayoutManager = new LinearLayoutManager(this);
        mIssuesRecyclerView.setLayoutManager(mIssuesLayoutManager);
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
        LabCoatApp.bus().unregister(mEventReceiver);
    }

    private void bind(Milestone milestone) {
        mToolbar.setTitle(milestone.getTitle());
        mMilestoneIssuesAdapter.setMilestone(milestone);
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
        GitLabClient.instance().getMilestoneIssues(mProject.getId(), mMilestone.getId()).enqueue(mIssuesCallback);
    }

    private void loadMore() {

        if (mNextPageUrl == null) {
            return;
        }

        mLoading = true;

        Timber.d("loadMore called for " + mNextPageUrl);
        GitLabClient.instance().getMilestoneIssues(mNextPageUrl.toString()).enqueue(mMoreIssuesCallback);
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
