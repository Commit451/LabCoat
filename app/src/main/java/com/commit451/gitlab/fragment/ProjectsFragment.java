package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.adapter.ProjectsAdapter;
import com.commit451.gitlab.api.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.NavigationManager;
import com.commit451.gitlab.util.PaginationUtil;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

public class ProjectsFragment extends ButtFragment {

    private static final String EXTRA_MODE = "extra_mode";
    private static final String EXTRA_QUERY = "extra_query";
    private static final String EXTRA_GROUP = "extra_group";

    public static final int MODE_ALL = 0;
    public static final int MODE_MINE = 1;
    public static final int MODE_STARRED = 2;
    public static final int MODE_SEARCH = 3;
    public static final int MODE_GROUP = 4;

    public static ProjectsFragment newInstance(int mode) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_MODE, mode);

        ProjectsFragment fragment = new ProjectsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ProjectsFragment newInstance(String searchTerm) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_MODE, MODE_SEARCH);
        args.putString(EXTRA_QUERY, searchTerm);
        ProjectsFragment fragment = new ProjectsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ProjectsFragment newInstance(Group group) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_MODE, MODE_GROUP);
        args.putParcelable(EXTRA_GROUP, Parcels.wrap(group));
        ProjectsFragment fragment = new ProjectsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list) RecyclerView mProjectsListView;
    @BindView(R.id.message_text) TextView mMessageView;

    private LinearLayoutManager mLayoutManager;
    private ProjectsAdapter mProjectsAdapter;

    private int mMode;
    private String mQuery;
    private Uri mNextPageUrl;
    private boolean mLoading = false;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMore();
            }
        }
    };

    private final EasyCallback<List<Project>> mProjectsCallback = new EasyCallback<List<Project>>() {
        @Override
        public void onResponse(@NonNull List<Project> response) {
            mLoading = false;
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (response.isEmpty()) {
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_projects);
            }
            mProjectsAdapter.setData(response);
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
            mMessageView.setText(R.string.connection_error);
            mProjectsAdapter.setData(null);
            mNextPageUrl = null;
        }
    };

    private final EasyCallback<List<Project>> mMoreProjectsCallback = new EasyCallback<List<Project>>() {
        @Override
        public void onResponse(@NonNull List<Project> response) {
            mLoading = false;
            if (getView() == null) {
                return;
            }
            mProjectsAdapter.setLoading(false);
            mProjectsAdapter.addData(response);
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
            mProjectsAdapter.setLoading(false);
        }
    };

    private final ProjectsAdapter.Listener mProjectsListener = new ProjectsAdapter.Listener() {
        @Override
        public void onProjectClicked(Project project) {
            NavigationManager.navigateToProject(getActivity(), project);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMode = getArguments().getInt(EXTRA_MODE);
        mQuery = getArguments().getString(EXTRA_QUERY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_projects, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProjectsAdapter = new ProjectsAdapter(getActivity(), mProjectsListener);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mProjectsListView.setLayoutManager(mLayoutManager);
        mProjectsListView.addItemDecoration(new DividerItemDecoration(getActivity()));
        mProjectsListView.setAdapter(mProjectsAdapter);
        mProjectsListView.addOnScrollListener(mOnScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        loadData();
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }
        mMessageView.setVisibility(View.GONE);

        mNextPageUrl = null;

        switch (mMode) {
            case MODE_ALL:
                showLoading();
                GitLabClient.instance().getAllProjects().enqueue(mProjectsCallback);
                break;
            case MODE_MINE:
                showLoading();
                GitLabClient.instance().getMyProjects().enqueue(mProjectsCallback);
                break;
            case MODE_STARRED:
                showLoading();
                GitLabClient.instance().getStarredProjects().enqueue(mProjectsCallback);
                break;
            case MODE_SEARCH:
                if (mQuery != null) {
                    showLoading();
                    GitLabClient.instance().searchAllProjects(mQuery).enqueue(mProjectsCallback);
                }
                break;
            case MODE_GROUP:
                showLoading();
                Group group = Parcels.unwrap(getArguments().getParcelable(EXTRA_GROUP));
                if (group == null) {
                    throw new IllegalStateException("You must also pass a group if you want to show a groups projects");
                }
                GitLabClient.instance().getGroupProjects(group.getId()).enqueue(mProjectsCallback);
                break;
            default:
                throw new IllegalStateException(mMode + " is not defined");
        }
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

        if (mNextPageUrl == null) {
            return;
        }
        mLoading = true;
        mProjectsAdapter.setLoading(true);
        Timber.d("loadMore called for " + mNextPageUrl);
        GitLabClient.instance().getProjects(mNextPageUrl.toString()).enqueue(mMoreProjectsCallback);
    }

    private void showLoading() {
        mLoading = true;
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
    }

    public void searchQuery(String query) {
        mQuery = query;

        if (mProjectsAdapter != null) {
            mProjectsAdapter.clearData();
            loadData();
        }
    }
}
