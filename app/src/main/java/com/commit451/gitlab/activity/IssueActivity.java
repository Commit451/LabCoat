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
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.rx.CustomSingleObserver;
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
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
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
    ViewGroup root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.issue_title)
    TextView textTitle;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listNotes;
    @BindView(R.id.send_message_view)
    SendMessageView sendMessageView;
    @BindView(R.id.progress)
    View progress;

    MenuItem menuItemOpenClose;
    IssueDetailsAdapter adapterIssueDetails;
    LinearLayoutManager layoutManagerNotes;

    Project project;
    Issue issue;
    String issueIid;
    boolean loading;
    Uri nextPageUrl;
    Teleprinter teleprinter;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManagerNotes.getChildCount();
            int totalItemCount = layoutManagerNotes.getItemCount();
            int firstVisibleItem = layoutManagerNotes.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMoreNotes();
            }
        }
    };

    private final Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_share:
                    IntentUtil.share(root, issue.getUrl(project));
                    return true;
                case R.id.action_close:
                    closeOrOpenIssue();
                    return true;
                case R.id.action_delete:
                    App.get().getGitLab().deleteIssue(project.getId(), issue.getId())
                            .compose(IssueActivity.this.<String>bindToLifecycle())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new CustomSingleObserver<String>() {

                                @Override
                                public void error(@NonNull Throwable t) {
                                    Timber.e(t);
                                    Snackbar.make(root, getString(R.string.failed_to_delete_issue), Snackbar.LENGTH_SHORT)
                                            .show();
                                }

                                @Override
                                public void success(@NonNull String s) {
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
        Navigator.navigateToEditIssue(IssueActivity.this, fab, project, issue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        ButterKnife.bind(this);
        teleprinter = new Teleprinter(this);
        App.bus().register(this);

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.menu_issue);
        menuItemOpenClose = toolbar.getMenu().findItem(R.id.action_close);
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener);

        layoutManagerNotes = new LinearLayoutManager(this);
        listNotes.setLayoutManager(layoutManagerNotes);
        listNotes.addOnScrollListener(onScrollListener);

        sendMessageView.setCallbacks(new SendMessageView.Callbacks() {
            @Override
            public void onSendClicked(String message) {
                postNote(message);
            }

            @Override
            public void onAttachmentClicked() {
                Navigator.navigateToAttach(IssueActivity.this, project, REQUEST_ATTACH);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNotes();
            }
        });

        if (getIntent().hasExtra(EXTRA_SELECTED_ISSUE)) {
            project = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));
            issue = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_SELECTED_ISSUE));
            adapterIssueDetails = new IssueDetailsAdapter(IssueActivity.this, issue, project);
            listNotes.setAdapter(adapterIssueDetails);
            bindIssue();
            bindProject();
            loadNotes();
        } else if (getIntent().hasExtra(EXTRA_ISSUE_IID)) {
            issueIid = getIntent().getStringExtra(EXTRA_ISSUE_IID);
            String projectNamespace = getIntent().getStringExtra(EXTRA_PROJECT_NAMESPACE);
            String projectName = getIntent().getStringExtra(EXTRA_PROJECT_NAME);
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                }
            });
            App.get().getGitLab().getProject(projectNamespace, projectName)
                    .flatMap(new Function<Project, SingleSource<List<Issue>>>() {
                        @Override
                        public SingleSource<List<Issue>> apply(Project project) throws Exception {
                            IssueActivity.this.project = project;
                            return App.get().getGitLab().getIssuesByIid(project.getId(), issueIid);
                        }
                    })
                    .compose(this.<List<Issue>>bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CustomSingleObserver<List<Issue>>() {

                        @Override
                        public void error(@NonNull Throwable t) {
                            Timber.e(t);
                            swipeRefreshLayout.setRefreshing(false);
                            Snackbar.make(root, getString(R.string.failed_to_load), Snackbar.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void success(@NonNull List<Issue> issues) {
                            if (issues.isEmpty()) {
                                swipeRefreshLayout.setRefreshing(false);
                                Snackbar.make(root, getString(R.string.failed_to_load), Snackbar.LENGTH_SHORT)
                                        .show();
                            } else {
                                issue = issues.get(0);
                                adapterIssueDetails = new IssueDetailsAdapter(IssueActivity.this, issue, project);
                                listNotes.setAdapter(adapterIssueDetails);
                                bindIssue();
                                bindProject();
                                loadNotes();
                            }
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
                    progress.setVisibility(View.GONE);
                    sendMessageView.appendText(response.getMarkdown());
                } else {
                    Snackbar.make(root, R.string.failed_to_upload_file, Snackbar.LENGTH_LONG)
                            .show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        App.bus().unregister(this);
        super.onDestroy();
    }

    private void bindProject() {
        toolbar.setSubtitle(project.getNameWithNamespace());
    }

    private void bindIssue() {
        toolbar.setTitle(getString(R.string.issue_number) + issue.getIid());
        setOpenCloseMenuStatus();
        textTitle.setText(issue.getTitle());
        adapterIssueDetails.updateIssue(issue);
    }

    private void loadNotes() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        loading = true;
        App.get().getGitLab().getIssueNotes(project.getId(), issue.getId())
                .compose(this.<Response<List<Note>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Note>>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        loading = false;
                        Timber.e(t);
                        swipeRefreshLayout.setRefreshing(false);
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Note> notes) {
                        loading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        adapterIssueDetails.setNotes(notes);
                    }
                });
    }

    private void loadMoreNotes() {
        loading = true;
        adapterIssueDetails.setLoading(true);
        App.get().getGitLab().getIssueNotes(nextPageUrl.toString())
                .compose(this.<Response<List<Note>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Note>>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        loading = false;
                        Timber.e(t);
                        adapterIssueDetails.setLoading(false);
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Note> notes) {
                        loading = false;
                        adapterIssueDetails.setLoading(false);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        adapterIssueDetails.addNotes(notes);
                    }
                });
    }

    private void postNote(String message) {

        if (message.length() < 1) {
            return;
        }

        progress.setVisibility(View.VISIBLE);
        progress.setAlpha(0.0f);
        progress.animate().alpha(1.0f);
        // Clear text & collapse keyboard
        teleprinter.hideKeyboard();
        sendMessageView.clearText();

        App.get().getGitLab().addIssueNote(project.getId(), issue.getId(), message)
                .compose(this.<Note>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Note>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        progress.setVisibility(View.GONE);
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void success(@NonNull Note note) {
                        progress.setVisibility(View.GONE);
                        adapterIssueDetails.addNote(note);
                        listNotes.smoothScrollToPosition(IssueDetailsAdapter.getHeaderCount());
                    }
                });
    }

    private void closeOrOpenIssue() {
        progress.setVisibility(View.VISIBLE);
        if (issue.getState().equals(Issue.STATE_CLOSED)) {
            updateIssueStatus(App.get().getGitLab().updateIssueStatus(project.getId(), issue.getId(), Issue.STATE_REOPEN));
        } else {
            updateIssueStatus(App.get().getGitLab().updateIssueStatus(project.getId(), issue.getId(), Issue.STATE_CLOSE));
        }
    }

    private void updateIssueStatus(Single<Issue> observable) {
        observable
                .compose(this.<Issue>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Issue>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        progress.setVisibility(View.GONE);
                        Snackbar.make(root, getString(R.string.error_changing_issue), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void success(@NonNull Issue issue) {
                        progress.setVisibility(View.GONE);
                        IssueActivity.this.issue = issue;
                        App.bus().post(new IssueChangedEvent(IssueActivity.this.issue));
                        App.bus().post(new IssueReloadEvent());
                        setOpenCloseMenuStatus();
                        loadNotes();
                    }
                });
    }

    private void setOpenCloseMenuStatus() {
        menuItemOpenClose.setTitle(issue.getState().equals(Issue.STATE_CLOSED) ? R.string.reopen : R.string.close);
    }

    @Subscribe
    public void onIssueChanged(IssueChangedEvent event) {
        if (issue.getId() == event.issue.getId()) {
            issue = event.issue;
            bindIssue();
            loadNotes();
        }
    }
}
