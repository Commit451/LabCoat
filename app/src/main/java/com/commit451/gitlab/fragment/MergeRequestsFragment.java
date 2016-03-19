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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.adapter.MergeRequestAdapter;
import com.commit451.gitlab.api.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.NavigationManager;
import com.commit451.gitlab.util.PaginationUtil;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MergeRequestsFragment extends BaseFragment {

    public static MergeRequestsFragment newInstance() {
        return new MergeRequestsFragment();
    }

    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mRecyclerView;
    @Bind(R.id.message_text) TextView mMessageView;
    @Bind(R.id.state_spinner) Spinner mSpinner;

    private Project mProject;
    private EventReceiver mEventReceiver;
    private MergeRequestAdapter mMergeRequestAdapter;
    private LinearLayoutManager mMergeLayoutManager;

    private String mState;
    private String[] mStates;
    private Uri mNextPageUrl;
    private boolean mLoading = false;

    private final AdapterView.OnItemSelectedListener mSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mState = mStates[position];
            loadData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private final MergeRequestAdapter.Listener mMergeRequestAdapterListener = new MergeRequestAdapter.Listener() {
        @Override
        public void onMergeRequestClicked(MergeRequest mergeRequest) {
            NavigationManager.navigateToMergeRequest(getActivity(), mProject, mergeRequest);
        }
    };

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mMergeLayoutManager.getChildCount();
            int totalItemCount = mMergeLayoutManager.getItemCount();
            int firstVisibleItem = mMergeLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMore();
            }
        }
    };

    private final EasyCallback<List<MergeRequest>> mCallback = new EasyCallback<List<MergeRequest>>() {
        @Override
        public void onResponse(@NonNull List<MergeRequest> response) {
            mLoading = false;
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (response.isEmpty()) {
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_merge_requests);
            }
            mMergeRequestAdapter.setData(response);
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
            mMessageView.setText(R.string.connection_error_merge_requests);
            mMergeRequestAdapter.setData(null);
            mNextPageUrl = null;
        }
    };

    private final EasyCallback<List<MergeRequest>> mMoreIssuesCallback = new EasyCallback<List<MergeRequest>>() {
        @Override
        public void onResponse(@NonNull List<MergeRequest> response) {
            mLoading = false;
            mMergeRequestAdapter.setLoading(false);
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
            mMergeRequestAdapter.addData(response);
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            mMergeRequestAdapter.setLoading(false);
            mLoading = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mState = getContext().getResources().getString(R.string.merge_request_state_value_default);
        mStates = getContext().getResources().getStringArray(R.array.merge_request_state_values);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merge_request, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mEventReceiver = new EventReceiver();
        LabCoatApp.bus().register(mEventReceiver);

        mMergeRequestAdapter = new MergeRequestAdapter(mMergeRequestAdapterListener);
        mMergeLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mMergeLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        mRecyclerView.setAdapter(mMergeRequestAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        mSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, getResources().getStringArray(R.array.merge_request_state_names)));
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
        ButterKnife.unbind(this);
        LabCoatApp.bus().unregister(mEventReceiver);
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
        GitLabClient.instance().getMergeRequests(mProject.getId(), mState).enqueue(mCallback);
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }
        if (mNextPageUrl == null) {
            return;
        }
        mMergeRequestAdapter.setLoading(true);
        mLoading = true;
        Timber.d("loadMore called for " + mNextPageUrl);
        GitLabClient.instance().getMergeRequests(mNextPageUrl.toString(), mState).enqueue(mMoreIssuesCallback);
    }

    private class EventReceiver {
        @Subscribe
        public void onProjectReload(ProjectReloadEvent event) {
            mProject = event.mProject;
            loadData();
        }
    }
}
