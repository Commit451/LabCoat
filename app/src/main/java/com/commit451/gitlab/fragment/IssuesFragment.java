package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.adapter.IssuesAdapter;
import com.commit451.gitlab.api.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.IssueChangedEvent;
import com.commit451.gitlab.event.IssueCreatedEvent;
import com.commit451.gitlab.event.IssueReloadEvent;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.NavigationManager;
import com.commit451.gitlab.util.PaginationUtil;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class IssuesFragment extends ButtFragment {

    public static IssuesFragment newInstance() {
        return new IssuesFragment();
    }

    @BindView(R.id.root)
    ViewGroup mRoot;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView mIssueListView;
    @BindView(R.id.message_text)
    TextView mMessageView;
    @BindView(R.id.issue_spinner)
    Spinner mSpinner;

    private Project mProject;
    private IssuesAdapter mIssuesAdapter;
    private LinearLayoutManager mIssuesLayoutManager;
    private EventReceiver mEventReceiver;

    String mState;
    private String[] mStates;
    private Uri mNextPageUrl;
    private boolean mLoading = false;

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

    private final IssuesAdapter.Listener mIssuesAdapterListener = new IssuesAdapter.Listener() {
        @Override
        public void onIssueClicked(Issue issue) {
            if (mProject != null) {
                NavigationManager.navigateToIssue(getActivity(), mProject, issue);
            } else {
                Snackbar.make(mRoot, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    };

    private final AdapterView.OnItemSelectedListener mSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mState = mStates[position];
            loadData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private final EasyCallback<List<Issue>> mIssuesCallback = new EasyCallback<List<Issue>>() {
        @Override
        public void onResponse(@NonNull List<Issue> response) {
            mLoading = false;
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (response.isEmpty()) {
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_issues);
            }
            mIssuesAdapter.setIssues(response);
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
            Timber.d("Next page url " + mNextPageUrl);
        }

        @Override
        public void onAllFailure(Throwable t) {
            mLoading = false;
            Timber.e(t, null);
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.connection_error_issues);
            mIssuesAdapter.setIssues(null);
            mNextPageUrl = null;
        }
    };

    private final EasyCallback<List<Issue>> mMoreIssuesCallback = new EasyCallback<List<Issue>>() {
        @Override
        public void onResponse(@NonNull List<Issue> response) {
            mLoading = false;
            mIssuesAdapter.setLoading(false);
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
            mIssuesAdapter.addIssues(response);
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            mLoading = false;
            mIssuesAdapter.setLoading(false);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mState = getResources().getString(R.string.issue_state_value_default);
        mStates = getResources().getStringArray(R.array.issue_state_values);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_issues, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEventReceiver = new EventReceiver();
        LabCoatApp.bus().register(mEventReceiver);

        mIssuesAdapter = new IssuesAdapter(mIssuesAdapterListener);
        mIssuesLayoutManager = new LinearLayoutManager(getActivity());
        mIssueListView.setLayoutManager(mIssuesLayoutManager);
        mIssueListView.addItemDecoration(new DividerItemDecoration(getActivity()));
        mIssueListView.setAdapter(mIssuesAdapter);
        mIssueListView.addOnScrollListener(mOnScrollListener);

        mSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, getResources().getStringArray(R.array.issue_state_names)));
        mSpinner.setOnItemSelectedListener(mSpinnerItemSelectedListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        if (getActivity() instanceof ProjectActivity) {
            mProject = ((ProjectActivity) getActivity()).getProject();
            loadData();
        } else {
            throw new IllegalStateException("Incorrect parent activity");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LabCoatApp.bus().unregister(mEventReceiver);
    }

    @OnClick(R.id.add_issue_button)
    public void onAddIssueClick(View fab) {
        if (mProject != null) {
            NavigationManager.navigateToAddIssue(getActivity(), fab, mProject);
        } else {
            Snackbar.make(mRoot, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }
        if (mProject == null) {
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }
        mMessageView.setVisibility(View.GONE);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        mNextPageUrl = null;
        mLoading = true;
        GitLabClient.instance().getIssues(mProject.getId(), mState).enqueue(mIssuesCallback);
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

        if (mNextPageUrl == null) {
            return;
        }

        mIssuesAdapter.setLoading(true);
        mLoading = true;

        Timber.d("loadMore called for " + mNextPageUrl);
        GitLabClient.instance().getIssues(mNextPageUrl.toString(), mState).enqueue(mMoreIssuesCallback);
    }

    private class EventReceiver {
        @Subscribe
        public void onProjectReload(ProjectReloadEvent event) {
            mProject = event.mProject;
            loadData();
        }

        @Subscribe
        public void onIssueCreated(IssueCreatedEvent event) {
            mIssuesAdapter.addIssue(event.mIssue);
            if (getView() != null) {
                mMessageView.setVisibility(View.GONE);
                mIssueListView.smoothScrollToPosition(0);
            }
        }

        @Subscribe
        public void onIssueChanged(IssueChangedEvent event) {
            mIssuesAdapter.updateIssue(event.mIssue);
        }

        @Subscribe
        public void onIssueReload(IssueReloadEvent event) {
            loadData();
        }
    }
}