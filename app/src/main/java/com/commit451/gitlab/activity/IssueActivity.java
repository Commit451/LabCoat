package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.IssueDetailsAdapter;
import com.commit451.gitlab.api.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.IssueChangedEvent;
import com.commit451.gitlab.event.IssueReloadEvent;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.IntentUtil;
import com.commit451.gitlab.util.KeyboardUtil;
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

    @Bind(R.id.root)
    ViewGroup mRoot;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.issue_title)
    TextView mIssueTitle;
    @Bind(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list)
    RecyclerView mNotesRecyclerView;
    @Bind(R.id.new_note_edit)
    EditText mNewNoteEdit;
    @Bind(R.id.progress)
    View mProgress;

    @OnClick(R.id.new_note_button)
    public void onNewNoteClick() {
        postNote();
    }

    @OnClick(R.id.fab_edit_issue)
    public void onEditIssueClick(View fab) {
        NavigationManager.navigateToEditIssue(IssueActivity.this, fab, mProject, mIssue);
    }

    private MenuItem mOpenCloseMenuItem;

    private IssueDetailsAdapter mIssueDetailsAdapter;
    private LinearLayoutManager mNotesLayoutManager;
    private Project mProject;
    private Issue mIssue;
    private boolean mLoading;
    private Uri mNextPageUrl;

    private EventReceiver mEventReceiver;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mNotesLayoutManager.getChildCount();
            int totalItemCount = mNotesLayoutManager.getItemCount();
            int firstVisibleItem = mNotesLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMoreNotes();
            }
        }
    };

    private final Toolbar.OnMenuItemClickListener mOnMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_share:
                    IntentUtil.share(mRoot, mIssue.getUrl(mProject));
                    return true;
                case R.id.action_close:
                    closeOrOpenIssue();
                    return true;
            }
            return false;
        }
    };

    private Callback<List<Note>> mNotesCallback = new EasyCallback<List<Note>>() {

        @Override
        public void onResponse(@NonNull List<Note> response) {
            mLoading = false;
            mSwipeRefreshLayout.setRefreshing(false);
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
            mIssueDetailsAdapter.setNotes(response);
        }

        @Override
        public void onAllFailure(Throwable t) {
            mLoading = false;
            Timber.e(t, null);
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private Callback<List<Note>> mMoreNotesCallback = new EasyCallback<List<Note>>() {

        @Override
        public void onResponse(@NonNull List<Note> response) {
            mLoading = false;
            mIssueDetailsAdapter.setLoading(false);
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
            mIssueDetailsAdapter.addNotes(response);
        }

        @Override
        public void onAllFailure(Throwable t) {
            mLoading = false;
            Timber.e(t, null);
            mIssueDetailsAdapter.setLoading(false);
        }
    };

    private final Callback<Issue> mOpenCloseCallback = new EasyCallback<Issue>() {
        @Override
        public void onResponse(@NonNull Issue response) {
            mProgress.setVisibility(View.GONE);
            mIssue = response;
            LabCoatApp.bus().post(new IssueChangedEvent(mIssue));
            LabCoatApp.bus().post(new IssueReloadEvent());
            setOpenCloseMenuStatus();
            loadNotes();
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, getString(R.string.error_changing_issue), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private Callback<Note> mPostNoteCallback = new EasyCallback<Note>() {

        @Override
        public void onResponse(@NonNull Note response) {
            mProgress.setVisibility(View.GONE);
            mIssueDetailsAdapter.addNote(response);
            mNotesRecyclerView.smoothScrollToPosition(IssueDetailsAdapter.getHeaderCount());
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        ButterKnife.bind(this);
        mEventReceiver = new EventReceiver();
        LabCoatApp.bus().register(mEventReceiver);

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
        mToolbar.inflateMenu(R.menu.menu_issue);
        mOpenCloseMenuItem = mToolbar.getMenu().findItem(R.id.action_close);
        mToolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);

        mIssueDetailsAdapter = new IssueDetailsAdapter(IssueActivity.this, mIssue);
        mNotesLayoutManager = new LinearLayoutManager(this);
        mNotesRecyclerView.setLayoutManager(mNotesLayoutManager);
        mNotesRecyclerView.setAdapter(mIssueDetailsAdapter);
        mNotesRecyclerView.addOnScrollListener(mOnScrollListener);

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
        LabCoatApp.bus().unregister(mEventReceiver);
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
        mLoading = true;
        GitLabClient.instance().getIssueNotes(mProject.getId(), mIssue.getId()).enqueue(mNotesCallback);
    }

    private void loadMoreNotes() {
        mLoading = true;
        mIssueDetailsAdapter.setLoading(true);
        GitLabClient.instance().getIssueNotes(mNextPageUrl.toString()).enqueue(mMoreNotesCallback);
    }

    private void postNote() {
        String body = mNewNoteEdit.getText().toString();

        if (body.length() < 1) {
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

    private void closeOrOpenIssue() {
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
            if (mIssue.getId() == event.mIssue.getId()) {
                mIssue = event.mIssue;
                bindIssue();
            }
        }
    }
}
