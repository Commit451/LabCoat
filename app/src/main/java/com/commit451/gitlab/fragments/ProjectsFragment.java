package com.commit451.gitlab.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.ProjectsAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.tools.LinkHeaderResolver;
import com.commit451.gitlab.tools.NavigationManager;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Shows a mFilesList of projects
 * Created by Jawn on 9/21/2015.
 */
public class ProjectsFragment extends BaseFragment {

    private static final String EXTRA_MODE = "extra_mode";
    private static final String EXTRA_QUERY = "extra_query";

    public static final int MODE_ALL = 0;
    public static final int MODE_MINE = 1;
    public static final int MODE_SEARCH = 2;

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

    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    ProjectsAdapter mProjectsAdapter;
    @Bind(R.id.message_text) TextView mMessageText;

    private int mMode;
    private String mQuery;
    private String mNextPageUrl;
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

    private final Callback<List<Project>> mProjectsCallback = new Callback<List<Project>>() {
        @Override
        public void onResponse(Response<List<Project>> response, Retrofit retrofit) {
            mLoading = false;
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (!response.isSuccess()) {
                mRecyclerView.setVisibility(View.GONE);
                mMessageText.setVisibility(View.VISIBLE);
                mMessageText.setText(R.string.connection_error);
                return;
            }
            mNextPageUrl = LinkHeaderResolver.getNextPageUrl(response.headers());
            Timber.d("Next page url " + mNextPageUrl);
            if (response.body().isEmpty()) {
                mMessageText.setText(R.string.no_projects);
                mRecyclerView.setVisibility(View.GONE);
                mMessageText.setVisibility(View.VISIBLE);
            } else {
                mMessageText.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mProjectsAdapter.addData(response.body());
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mMessageText.setVisibility(View.VISIBLE);
            mMessageText.setText(R.string.connection_error);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_projects, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        mMessageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
        mProjectsAdapter = new ProjectsAdapter(getActivity(), mProjectsListener);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mProjectsAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        loadData();
    }

    @Override
    protected void loadData() {
        super.loadData();
        mMessageText.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mProjectsAdapter.clearData();
        switch (mMode) {
            case MODE_ALL:
                showLoading();
                GitLabClient.instance().getAllProjects().enqueue(mProjectsCallback);
                break;
            case MODE_MINE:
                showLoading();
                GitLabClient.instance().getMyProjects().enqueue(mProjectsCallback);
                break;
            case MODE_SEARCH:
                if (mQuery != null) {
                    showLoading();
                    GitLabClient.instance().searchAllProjects(mQuery).enqueue(mProjectsCallback);
                }
                break;
            default:
                throw new IllegalStateException("this mode is not defined");

        }
    }

    private void loadMore() {
        mLoading = true;
        Timber.d("loadMore called for " + mNextPageUrl);
        GitLabClient.instance().getProjectsNextPage(mNextPageUrl).enqueue(mProjectsCallback);
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
        if (mProjectsAdapter != null) {
            mProjectsAdapter.clearData();
        }
        mQuery = query;
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
