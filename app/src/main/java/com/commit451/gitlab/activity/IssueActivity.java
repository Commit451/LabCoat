package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.IssueDetailsAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.dialog.AssigneeDialog;
import com.commit451.gitlab.event.IssueChangedEvent;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.IntentUtil;
import com.commit451.gitlab.util.KeyboardUtil;
import com.commit451.gitlab.util.NavigationManager;
import com.squareup.otto.Subscribe;

import org.parceler.Parcels;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Shows off an issue like a bar of gold
 */
public class IssueActivity extends BaseActivity {

    private static final String EXTRA_PROJECT = "extra_project";
    private static final String EXTRA_SELECTED_ISSUE = "extra_selected_issue";

    public static Intent newInstance(Context context, Project project, Issue issue) {
        Intent intent = new Intent(context, IssueActivity.class);
        intent.putExtra(EXTRA_PROJECT, Parcels.wrap(project));
        intent.putExtra(EXTRA_SELECTED_ISSUE, Parcels.wrap(issue));
        return intent;
    }

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.issue_title) TextView mIssueTitle;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mListView;
    @Bind(R.id.new_note_edit) EditText mNewNoteEdit;
    @Bind(R.id.progress) View mProgress;

    @OnClick(R.id.new_note_button)
    public void onNewNoteClick() {
        postNote();
    }

    @OnClick(R.id.fab_edit_issue)
    public void onEditIssueClick() {
        NavigationManager.navigateToEditIssue(IssueActivity.this, mProject, mIssue);
    }

    MenuItem mOpenCloseMenuItem;

    IssueDetailsAdapter mIssueDetailsAdapter;
    Project mProject;
    Issue mIssue;

    EventReceiver mEventReceiver;

    private final Toolbar.OnMenuItemClickListener mOnMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_share:
                    IntentUtil.share(getWindow().getDecorView(), mIssue.getUrl(mProject));
                    return true;
                case R.id.action_close:
                    closeIssue();
                    return true;
                case R.id.action_assign:
                    new AssigneeDialog(IssueActivity.this, mIssue.getAssignee(), mProject).show();
                    return true;
            }
            return false;
        }
    };

    private Callback<List<Note>> mNotesCallback = new Callback<List<Note>>() {

        @Override
        public void onResponse(Response<List<Note>> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            //Annoying that this is not API controlled...
            Collections.reverse(response.body());
            mIssueDetailsAdapter.addNotes(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private final Callback<Issue> mOpenCloseCallback = new Callback<Issue>() {
        @Override
        public void onResponse(Response<Issue> response, Retrofit retrofit) {
            mProgress.setVisibility(View.GONE);
            if (!response.isSuccess()) {
                Snackbar.make(getWindow().getDecorView(), getString(R.string.error_changing_issue), Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            mIssue = response.body();
            GitLabApp.bus().post(new IssueChangedEvent(mIssue));
            setOpenCloseMenuStatus();
            loadNotes();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(getWindow().getDecorView(), getString(R.string.error_changing_issue), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private Callback<Note> mPostNoteCallback = new Callback<Note>() {

        @Override
        public void onResponse(Response<Note> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                return;
            }
            mProgress.setVisibility(View.GONE);
            mIssueDetailsAdapter.addNote(response.body());
            mListView.smoothScrollToPosition(mIssueDetailsAdapter.getItemCount());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        ButterKnife.bind(this);
        mEventReceiver = new EventReceiver();
        GitLabApp.bus().register(mEventReceiver);

        mProject = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));
        mIssue = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_SELECTED_ISSUE));

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setSubtitle(mProject.getNameWithNamespace());
        mToolbar.inflateMenu(R.menu.issue);
        mOpenCloseMenuItem = mToolbar.getMenu().findItem(R.id.action_close);
        mToolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);

        mIssueDetailsAdapter = new IssueDetailsAdapter(mIssue);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mListView.setAdapter(mIssueDetailsAdapter);

        mNewNoteEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                postNote();
                return true;
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNotes();
            }
        });
        bindIssue();
        loadNotes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GitLabApp.bus().unregister(mEventReceiver);
    }

    private void bindIssue() {
        mToolbar.setTitle(getString(R.string.issue_number) + mIssue.getIid());
        setOpenCloseMenuStatus();
        mIssueTitle.setText(mIssue.getTitle());
        mIssueDetailsAdapter.updateIssue(mIssue);
    }

    private void loadNotes() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        mSwipeRefreshLayout.setRefreshing(true);
        GitLabClient.instance().getIssueNotes(mProject.getId(), mIssue.getId()).enqueue(mNotesCallback);
    }

    private void postNote() {
        String body = mNewNoteEdit.getText().toString();

        if(body.length() < 1) {
            return;
        }

        mProgress.setVisibility(View.VISIBLE);
        mProgress.setAlpha(0.0f);
        mProgress.animate().alpha(1.0f);
        // Clear text & collapse keyboard
        KeyboardUtil.hideKeyboard(this);
        mNewNoteEdit.setText("");

        GitLabClient.instance().addIssueNote(mProject.getId(), mIssue.getId(), body).enqueue(mPostNoteCallback);
    }

    private void closeIssue() {
        mProgress.setVisibility(View.VISIBLE);
        if (mIssue.getState() == Issue.State.CLOSED) {
            GitLabClient.instance().updateIssueStatus(mProject.getId(), mIssue.getId(), Issue.STATE_REOPEN)
                .enqueue(mOpenCloseCallback);
        } else {
            GitLabClient.instance().updateIssueStatus(mProject.getId(), mIssue.getId(), Issue.STATE_CLOSE)
                    .enqueue(mOpenCloseCallback);
        }
    }

    private void setOpenCloseMenuStatus() {
        mOpenCloseMenuItem.setTitle(mIssue.getState() == Issue.State.CLOSED ? R.string.reopen : R.string.close);
    }

    private class EventReceiver {

        @Subscribe
        public void onIssueChanged(IssueChangedEvent event) {
            if (mIssue.getId() == event.issue.getId()) {
                mIssue = event.issue;
                bindIssue();
            }
        }
    }
}
