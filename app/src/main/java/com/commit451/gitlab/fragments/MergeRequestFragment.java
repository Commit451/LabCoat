package com.commit451.gitlab.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activities.ProjectActivity;
import com.commit451.gitlab.adapter.MergeRequestAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.events.ProjectReloadEvent;
import com.commit451.gitlab.model.MergeRequest;
import com.commit451.gitlab.model.Project;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Jawn on 9/20/2015.
 */
public class MergeRequestFragment extends BaseFragment {

    public static MergeRequestFragment newInstance() {

        Bundle args = new Bundle();

        MergeRequestFragment fragment = new MergeRequestFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.error_text) TextView mErrorText;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mRecyclerView;
    EventReceiver mEventReceiver;
    MergeRequestAdapter mMergeRequestAdapter;
    Project mProject;

    private final SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            loadData();
        }
    };

    private final Callback<List<MergeRequest>> mCallback = new Callback<List<MergeRequest>>() {
        @Override
        public void onResponse(Response<List<MergeRequest>> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                return;
            }
            if (getView() != null) {
                mSwipeRefreshLayout.setRefreshing(false);
                if (response.body().isEmpty()) {
                    mErrorText.setVisibility(View.VISIBLE);
                    mErrorText.setText(R.string.no_merge_requests);
                } else {
                    mMergeRequestAdapter.setData(response.body());
                }
            }
        }

        @Override
        public void onFailure(Throwable t) {
            if (getView() != null) {
                mSwipeRefreshLayout.setRefreshing(false);
                Snackbar.make(getView(), R.string.connection_error, Snackbar.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventReceiver = new EventReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merge_request, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        GitLabApp.bus().register(mEventReceiver);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mMergeRequestAdapter = new MergeRequestAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mMergeRequestAdapter);
        if (getActivity() instanceof ProjectActivity) {
            mProject = ((ProjectActivity) getActivity()).getProject();
            if (mProject != null) {
                loadData();
            }
        } else {
            throw new IllegalStateException("Incorrect parent activity");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GitLabApp.bus().unregister(mEventReceiver);
        ButterKnife.unbind(this);
    }

    @Override
    protected void loadData() {
        super.loadData();
        mSwipeRefreshLayout.setRefreshing(true);
        GitLabClient.instance().getMergeRequests(mProject.getId()).enqueue(mCallback);
    }

    public boolean onBackPressed() {
        return false;
    }

    private class EventReceiver {

        @Subscribe
        public void onProjectChanged(ProjectReloadEvent event) {
            mProject = event.project;
            loadData();
        }
    }
}
