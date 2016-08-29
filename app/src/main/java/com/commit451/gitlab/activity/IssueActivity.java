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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.IssueDetailsAdapter;
import com.commit451.gitlab.event.IssueChangedEvent;
import com.commit451.gitlab.event.IssueReloadEvent;
import com.commit451.gitlab.model.api.FileUploadResponse;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.util.IntentUtil;
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.gitlab.view.SendMessageView;
import com.commit451.teleprinter.Teleprinter;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Shows off an issue like a bar of gold
 */
public class IssueActivity extends BaseActivity {

    private static final String EXTRA_PROJECT = "extra_project";
    private static final String EXTRA_SELECTED_ISSUE = "extra_selected_issue";
    private static final String EXTRA_PROJECT_NAMESPACE = "project_namespace";
    private static final String EXTRA_PROJECT_NAME = "project_name";
    private static final String EXTRA_ISSUE_IID = "extra_issue_iid";

    private static final int REQUEST_ATTACH = 1;

    public static Intent newIntent(Context context, Project project, Issue issue) {
        Intent intent = new Intent(context, IssueActivity.class);
        intent.putExtra(EXTRA_PROJECT, Parcels.wrap(project));
        intent.putExtra(EXTRA_SELECTED_ISSUE, Parcels.wrap(issue));
        return intent;
    }

    public static Intent newIntent(Context context, String namespace, String projectName, String issueIid) {
        Intent intent = new Intent(context, IssueActivity.class);
        intent.putExtra(EXTRA_PROJECT_NAMESPACE, namespace);
        intent.putExtra(EXTRA_PROJECT_NAME, projectName);
        intent.putExtra(EXTRA_ISSUE_IID, issueIid);
        return intent;
    }

    @BindView(R.id.root)
    ViewGroup mRoot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.issue_title)
    TextView mIssueTitle;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView mNotesRecyclerView;
    @BindView(R.id.send_message_view)
    SendMessageView mSendMessageView;
    @BindView(R.id.progress)
    View mProgress;

    MenuItem mOpenCloseMenuItem;
    IssueDetailsAdapter mIssueDetailsAdapter;
    LinearLayoutManager mNotesLayoutManager;

    Project mProject;
    Issue mIssue;
    String mIssueIid;
    boolean mLoading;
    Uri mNextPageUrl;
    Teleprinter mTeleprinter;

    EventReceiver mEventReceiver;

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
                case R.id.action_delete:
                    App.instance().getGitLab().deleteIssue(mProject.getId(), mIssue.getId()).enqueue(mDeleteIssueCallback);
                    return true;
            }
            return false;
        }
    };

    private Callback<Project> mProjectCallback = new EasyCallback<Project>() {
        @Override
        public void success(@NonNull Project response) {
            mProject = response;
            App.instance().getGitLab().getIssuesByIid(mProject.getId(), mIssueIid).enqueue(mIssueCallback);
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(mRoot, getString(R.string.failed_to_load), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private Callback<List<Issue>> mIssueCallback = new EasyCallback<List<Issue>>() {

        @Override
        public void success(@NonNull List<Issue> response) {
            if (response.isEmpty()) {
                mSwipeRefreshLayout.setRefreshing(false);
                Snackbar.make(mRoot, getString(R.string.failed_to_load), Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                mIssue = response.get(0);
                mIssueDetailsAdapter = new IssueDetailsAdapter(IssueActivity.this, mIssue, mProject);
                mNotesRecyclerView.setAdapter(mIssueDetailsAdapter);
                bindIssue();
                bindProject();
                loadNotes();
            }
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(mRoot, getString(R.string.failed_to_load), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private Callback<List<Note>> mNotesCallback = new EasyCallback<List<Note>>() {

        @Override
        public void success(@NonNull List<Note> response) {
            mLoading = false;
            mSwipeRefreshLayout.setRefreshing(false);
            mNextPageUrl = LinkHeaderParser.parse(getResponse()).getNext();
            mIssueDetailsAdapter.setNotes(response);
        }

        @Override
        public void failure(Throwable t) {
            mLoading = false;
            Timber.e(t, null);
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private Callback<List<Note>> mMoreNotesCallback = new EasyCallback<List<Note>>() {

        @Override
        public void success(@NonNull List<Note> response) {
            mLoading = false;
            mIssueDetailsAdapter.setLoading(false);
            mNextPageUrl = LinkHeaderParser.parse(getResponse()).getNext();
            mIssueDetailsAdapter.addNotes(response);
        }

        @Override
        public void failure(Throwable t) {
            mLoading = false;
            Timber.e(t, null);
            mIssueDetailsAdapter.setLoading(false);
        }
    };

    private final Callback<Issue> mOpenCloseCallback = new EasyCallback<Issue>() {
        @Override
        public void success(@NonNull Issue response) {
            mProgress.setVisibility(View.GONE);
            mIssue = response;
            App.bus().post(new IssueChangedEvent(mIssue));
            App.bus().post(new IssueReloadEvent());
            setOpenCloseMenuStatus();
            loadNotes();
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, getString(R.string.error_changing_issue), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private Callback<Note> mPostNoteCallback = new EasyCallback<Note>() {

        @Override
        public void success(@NonNull Note response) {
            mProgress.setVisibility(View.GONE);
            mIssueDetailsAdapter.addNote(response);
            mNotesRecyclerView.smoothScrollToPosition(IssueDetailsAdapter.getHeaderCount());
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private Callback<FileUploadResponse> mUploadImageCallback = new EasyCallback<FileUploadResponse>() {
        @Override
        public void success(@NonNull FileUploadResponse response) {

        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private final Callback<Void> mDeleteIssueCallback = new Callback<Void>() {
        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            App.bus().post(new IssueReloadEvent());
            Toast.makeText(IssueActivity.this, R.string.issue_deleted, Toast.LENGTH_SHORT)
                    .show();
            finish();
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            Timber.e(t, null);
            Snackbar.make(mRoot, getString(R.string.failed_to_delete_issue), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    @OnClick(R.id.fab_edit_issue)
    public void onEditIssueClick(View fab) {
        Navigator.navigateToEditIssue(IssueActivity.this, fab, mProject, mIssue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        ButterKnife.bind(this);
        mTeleprinter = new Teleprinter(this);
        mEventReceiver = new EventReceiver();
        App.bus().register(mEventReceiver);

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.inflateMenu(R.menu.menu_issue);
        mOpenCloseMenuItem = mToolbar.getMenu().findItem(R.id.action_close);
        mToolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);

        mNotesLayoutManager = new LinearLayoutManager(this);
        mNotesRecyclerView.setLayoutManager(mNotesLayoutManager);
        mNotesRecyclerView.addOnScrollListener(mOnScrollListener);

        mSendMessageView.setCallbacks(new SendMessageView.Callbacks() {
            @Override
            public void onSendClicked(String message) {
                postNote(message);
            }

            @Override
            public void onAttachmentClicked() {
                Navigator.navigateToAttach(IssueActivity.this, mProject, REQUEST_ATTACH);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNotes();
            }
        });

        if (getIntent().hasExtra(EXTRA_SELECTED_ISSUE)) {
            mProject = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));
            mIssue = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_SELECTED_ISSUE));
            mIssueDetailsAdapter = new IssueDetailsAdapter(IssueActivity.this, mIssue, mProject);
            mNotesRecyclerView.setAdapter(mIssueDetailsAdapter);
            bindIssue();
            bindProject();
            loadNotes();
        } else if (getIntent().hasExtra(EXTRA_ISSUE_IID)) {
            mIssueIid = getIntent().getStringExtra(EXTRA_ISSUE_IID);
            String projectNamespace = getIntent().getStringExtra(EXTRA_PROJECT_NAMESPACE);
            String projectName = getIntent().getStringExtra(EXTRA_PROJECT_NAME);
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                }
            });
            App.instance().getGitLab().getProject(projectNamespace, projectName).enqueue(mProjectCallback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ATTACH:
                if (resultCode == RESULT_OK) {
                    FileUploadResponse response = Parcels.unwrap(data.getParcelableExtra(AttachActivity.KEY_FILE_UPLOAD_RESPONSE));
                    mProgress.setVisibility(View.GONE);
                    mSendMessageView.appendText(response.getMarkdown());
                } else {
                  Snackbar.make(mRoot, R.string.failed_to_upload_file, Snackbar.LENGTH_LONG)
                          .show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.bus().unregister(mEventReceiver);
    }

    private void bindProject() {
        mToolbar.setSubtitle(mProject.getNameWithNamespace());
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
        App.instance().getGitLab().getIssueNotes(mProject.getId(), mIssue.getId()).enqueue(mNotesCallback);
    }

    private void loadMoreNotes() {
        mLoading = true;
        mIssueDetailsAdapter.setLoading(true);
        App.instance().getGitLab().getIssueNotes(mNextPageUrl.toString()).enqueue(mMoreNotesCallback);
    }

    private void postNote(String message) {

        if (message.length() < 1) {
            return;
        }

        mProgress.setVisibility(View.VISIBLE);
        mProgress.setAlpha(0.0f);
        mProgress.animate().alpha(1.0f);
        // Clear text & collapse keyboard
        mTeleprinter.hideKeyboard();
        mSendMessageView.clearText();

        App.instance().getGitLab().addIssueNote(mProject.getId(), mIssue.getId(), message).enqueue(mPostNoteCallback);
    }

    private void closeOrOpenIssue() {
        mProgress.setVisibility(View.VISIBLE);
        if (mIssue.getState().equals(Issue.STATE_CLOSED)) {
            App.instance().getGitLab().updateIssueStatus(mProject.getId(), mIssue.getId(), Issue.STATE_REOPEN)
                    .enqueue(mOpenCloseCallback);
        } else {
            App.instance().getGitLab().updateIssueStatus(mProject.getId(), mIssue.getId(), Issue.STATE_CLOSE)
                    .enqueue(mOpenCloseCallback);
        }
    }

    private void setOpenCloseMenuStatus() {
        mOpenCloseMenuItem.setTitle(mIssue.getState().equals(Issue.STATE_CLOSED) ? R.string.reopen : R.string.close);
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
