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

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.DiffAdapter;
import com.commit451.gitlab.model.api.Diff;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.rx.CustomSingleObserver;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
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

    @BindView(R.id.root)
    ViewGroup root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listDiff;
    @BindView(R.id.message_text)
    TextView textMessage;

    DiffAdapter adapterDiff;

    private Project project;
    private RepositoryCommit commit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff);
        ButterKnife.bind(this);

        project = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));
        commit = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_COMMIT));

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(commit.getShortId());

        adapterDiff = new DiffAdapter(commit, new DiffAdapter.Listener() {
            @Override
            public void onDiffClicked(Diff diff) {

            }
        });
        listDiff.setAdapter(adapterDiff);
        listDiff.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        loadData();
    }

    private void loadData() {
        textMessage.setVisibility(View.GONE);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        App.get().getGitLab().getCommitDiff(project.getId(), commit.getId())
                .compose(this.<List<Diff>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<List<Diff>>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        Timber.e(t);
                        textMessage.setText(R.string.connection_error);
                        textMessage.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void success(@NonNull List<Diff> diffs) {
                        swipeRefreshLayout.setRefreshing(false);
                        adapterDiff.setData(diffs);
                    }
                });
    }
}