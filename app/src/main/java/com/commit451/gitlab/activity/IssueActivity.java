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
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
                    App.get().getGitLab().deleteIssue(mProject.getId(), mIssue.getId())
                            .compose(IssueActivity.this.<String>bindToLifecycle())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<String>() {
                                @Override
                                public void onCompleted() {
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Timber.e(e);
                                    Snackbar.make(mRoot, getString(R.string.failed_to_delete_issue), Snackbar.LENGTH_SHORT)
                                            .show();
                                }

                                @Override
                                public void onNext(String s) {
                                    App.bus().post(new IssueReloadEvent());
                                    Toast.makeText(IssueActivity.this, R.string.issue_deleted, Toast.LENGTH_SHORT)
                                            .show();
                                    finish();
                                }
                            });
                    return true;
            }
            return false;
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
            App.get().getGitLab().getProject(projectNamespace, projectName)
                    .compose(this.<Project>bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Project>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.e(e);
                            mSwipeRefreshLayout.setRefreshing(false);
                            Snackbar.make(mRoot, getString(R.string.failed_to_load), Snackbar.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void onNext(Project project) {
                            mProject = project;
                            App.get().getGitLab().getIssuesByIid(mProject.getId(), mIssueIid)
                                    .compose(IssueActivity.this.<List<Issue>>bindToLifecycle())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Subscriber<List<Issue>>() {
                                        @Override
                                        public void onCompleted() {
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Timber.e(e);
                                            mSwipeRefreshLayout.setRefreshing(false);
                                            Snackbar.make(mRoot, getString(R.string.failed_to_load), Snackbar.LENGTH_SHORT)
                                                    .show();
                                        }

                                        @Override
                                        public void onNext(List<Issue> issues) {
                                            if (issues.isEmpty()) {
                                                mSwipeRefreshLayout.setRefreshing(false);
                                                Snackbar.make(mRoot, getString(R.string.failed_to_load), Snackbar.LENGTH_SHORT)
                                                        .show();
                                            } else {
                                                mIssue = issues.get(0);
                                                mIssueDetailsAdapter = new IssueDetailsAdapter(IssueActivity.this, mIssue, mProject);
                                                mNotesRecyclerView.setAdapter(mIssueDetailsAdapter);
                                                bindIssue();
                                                bindProject();
                                                loadNotes();
                                            }
                                        }
                                    });
                        }
                    });
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
        App.get().getGitLab().getIssueNotes(mProject.getId(), mIssue.getId())
                .compose(this.<Response<List<Note>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<List<Note>>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        mLoading = false;
                        Timber.e(e);
                        mSwipeRefreshLayout.setRefreshing(false);
                        Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onNext(Response<List<Note>> listResponse) {
                        if (!listResponse.isSuccessful()) {
                            onError(new HttpException(listResponse));
                            return;
                        }
                        mLoading = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                        mNextPageUrl = LinkHeaderParser.parse(listResponse).getNext();
                        mIssueDetailsAdapter.setNotes(listResponse.body());
                    }
                });
    }

    private void loadMoreNotes() {
        mLoading = true;
        mIssueDetailsAdapter.setLoading(true);
        App.get().getGitLab().getIssueNotes(mNextPageUrl.toString())
                .compose(this.<Response<List<Note>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<List<Note>>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        mLoading = false;
                        Timber.e(e);
                        mIssueDetailsAdapter.setLoading(false);
                    }

                    @Override
                    public void onNext(Response<List<Note>> listResponse) {
                        if (!listResponse.isSuccessful()) {
                            onError(new HttpException(listResponse));
                            return;
                        }
                        mLoading = false;
                        mIssueDetailsAdapter.setLoading(false);
                        mNextPageUrl = LinkHeaderParser.parse(listResponse).getNext();
                        mIssueDetailsAdapter.addNotes(listResponse.body());
                    }
                });
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

        App.get().getGitLab().addIssueNote(mProject.getId(), mIssue.getId(), message)
                .compose(this.<Note>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Note>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mProgress.setVisibility(View.GONE);
                        Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onNext(Note note) {
                        mProgress.setVisibility(View.GONE);
                        mIssueDetailsAdapter.addNote(note);
                        mNotesRecyclerView.smoothScrollToPosition(IssueDetailsAdapter.getHeaderCount());
                    }
                });
    }

    private void closeOrOpenIssue() {
        mProgress.setVisibility(View.VISIBLE);
        if (mIssue.getState().equals(Issue.STATE_CLOSED)) {
            updateIssueStatus(App.get().getGitLab().updateIssueStatus(mProject.getId(), mIssue.getId(), Issue.STATE_REOPEN));
        } else {
            updateIssueStatus(App.get().getGitLab().updateIssueStatus(mProject.getId(), mIssue.getId(), Issue.STATE_CLOSE));
        }
    }

    private void updateIssueStatus(Observable<Issue> observable) {
        observable
                .compose(this.<Issue>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Issue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mProgress.setVisibility(View.GONE);
                        Snackbar.make(mRoot, getString(R.string.error_changing_issue), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onNext(Issue issue) {
                        mProgress.setVisibility(View.GONE);
                        mIssue = issue;
                        App.bus().post(new IssueChangedEvent(mIssue));
                        App.bus().post(new IssueReloadEvent());
                        setOpenCloseMenuStatus();
                        loadNotes();
                    }
                });
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
