package com.commit451.gitlab.fragment;

import android.os.Bundle;
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

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.MergeRequestAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.NavigationManager;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
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

    @BindString(R.string.merge_request_state_value_default)
    String mState;
    private String[] mStates;
    private int mPage;
    private boolean mLoading = false;
    private boolean mDoneLoading;

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
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && !mDoneLoading) {
                loadMore();
            }
        }
    };

    private final Callback<List<MergeRequest>> mCallback = new Callback<List<MergeRequest>>() {
        @Override
        public void onResponse(Response<List<MergeRequest>> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }

            if (!response.isSuccess()) {
                Timber.e("Merge requests response was not a success: %d", response.code());
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.connection_error_merge_requests);
                mMergeRequestAdapter.setData(null);
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mLoading = false;

            if (!response.body().isEmpty()) {
                mMessageView.setVisibility(View.GONE);
            } else {
                Timber.d("No merge requests found");
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_merge_requests);
            }

            mMergeRequestAdapter.setData(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);

            if (getView() == null) {
                return;
            }
            mLoading = false;

            mSwipeRefreshLayout.setRefreshing(false);

            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.connection_error);
            mMergeRequestAdapter.setData(null);
        }
    };

    private final Callback<List<MergeRequest>> mMoreCallback = new Callback<List<MergeRequest>>() {
        @Override
        public void onResponse(Response<List<MergeRequest>> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }

            mLoading = false;
            mMergeRequestAdapter.addData(response.body());
            if (response.body().isEmpty()) {
                mDoneLoading = true;
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);

            if (getView() == null) {
                return;
            }
            mLoading = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        GitLabApp.bus().register(mEventReceiver);

        mMergeRequestAdapter = new MergeRequestAdapter(mMergeRequestAdapterListener);
        mMergeLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mMergeLayoutManager);
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
        GitLabApp.bus().unregister(mEventReceiver);
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }

        if (mProject == null) {
            return;
        }

        mPage = 1;
        mLoading = true;
        mDoneLoading = false;

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        GitLabClient.instance().getMergeRequests(mProject.getId(), mState, mPage).enqueue(mCallback);
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }
        mPage++;
        mLoading = true;
        Timber.d("loadMore called for " + mPage);
        GitLabClient.instance().getMergeRequests(mProject.getId(), mState, mPage).enqueue(mMoreCallback);
    }

    private class EventReceiver {
        @Subscribe
        public void onProjectReload(ProjectReloadEvent event) {
            mProject = event.mProject;
            loadData();
        }
    }
}
