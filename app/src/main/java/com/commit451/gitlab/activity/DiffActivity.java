package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.DiffAdapter;
import com.commit451.gitlab.model.api.Diff;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Callback;
import timber.log.Timber;

/**
 * Shows the lines of a commit aka the diff
 */
public class DiffActivity extends BaseActivity {

    private static final String EXTRA_PROJECT = "extra_project";
    private static final String EXTRA_COMMIT = "extra_commit";

    public static Intent newIntent(Context context, Project project, RepositoryCommit commit) {
        Intent intent = new Intent(context, DiffActivity.class);
        intent.putExtra(EXTRA_PROJECT, Parcels.wrap(project));
        intent.putExtra(EXTRA_COMMIT, Parcels.wrap(commit));
        return intent;
    }

    @BindView(R.id.root) ViewGroup mRoot;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list) RecyclerView mDiffRecyclerView;
    DiffAdapter mDiffAdapter;
    @BindView(R.id.message_text) TextView mMessageText;

    private Project mProject;
    private RepositoryCommit mCommit;

    private Callback<List<Diff>> mDiffCallback = new EasyCallback<List<Diff>>() {
        @Override
        public void success(@NonNull List<Diff> response) {
            mSwipeRefreshLayout.setRefreshing(false);
            mDiffAdapter.setData(response);
        }

        @Override
        public void failure(Throwable t) {
            mSwipeRefreshLayout.setRefreshing(false);
            Timber.e(t);
            mMessageText.setText(R.string.connection_error);
            mMessageText.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff);
        ButterKnife.bind(this);

        mProject = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));
        mCommit = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_COMMIT));

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setTitle(mCommit.getShortId());

        mDiffAdapter = new DiffAdapter(mCommit, new DiffAdapter.Listener() {
            @Override
            public void onDiffClicked(Diff diff) {

            }
        });
        mDiffRecyclerView.setAdapter(mDiffAdapter);
        mDiffRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        loadData();
    }

    private void loadData() {
        mMessageText.setVisibility(View.GONE);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(true);
            }
            }
        });
        App.instance().getGitLab().getCommitDiff(mProject.getId(), mCommit.getId()).enqueue(mDiffCallback);
    }
}