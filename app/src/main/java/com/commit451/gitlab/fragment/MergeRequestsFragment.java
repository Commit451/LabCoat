package com.commit451.gitlab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.MergeRequestAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.MergeRequest;
import com.commit451.gitlab.model.Project;
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

/**
 * Merge all the requests!
 * Created by Jawn on 9/20/2015.
 */
public class MergeRequestsFragment extends BaseFragment {

    public static MergeRequestsFragment newInstance() {
        Bundle args = new Bundle();
        MergeRequestsFragment fragment = new MergeRequestsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.error_text) TextView mErrorText;
    @Bind(R.id.state_spinner) Spinner mSpinner;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mRecyclerView;

    EventReceiver mEventReceiver;
    MergeRequestAdapter mMergeRequestAdapter;
    Project mProject;
    @BindString(R.string.merge_request_state_value_default)
    String mState;
    String[] mStates;

    private final SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            loadData();
        }
    };

    private final AdapterView.OnItemSelectedListener mSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mState = mStates[position];
            loadData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    };

    private final MergeRequestAdapter.Listener mMergeRequestAdapterListener = new MergeRequestAdapter.Listener() {
        @Override
        public void onMergeRequestClicked(MergeRequest mergeRequest) {
            NavigationManager.navigateToMergeRequest(getActivity(), mProject, mergeRequest);
        }
    };

    private final Callback<List<MergeRequest>> mCallback = new Callback<List<MergeRequest>>() {
        @Override
        public void onResponse(Response<List<MergeRequest>> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                return;
            }
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (response.body().isEmpty()) {
                mErrorText.setVisibility(View.VISIBLE);
                mErrorText.setText(R.string.no_merge_requests);
            }
            mMergeRequestAdapter.setData(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(getView(), R.string.connection_error, Snackbar.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventReceiver = new EventReceiver();
        mStates = getContext().getResources().getStringArray(R.array.merge_request_state_values);
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
        mMergeRequestAdapter = new MergeRequestAdapter(mMergeRequestAdapterListener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mMergeRequestAdapter);
        mSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, getResources().getStringArray(R.array.merge_request_state_names)));
        mSpinner.setOnItemSelectedListener(mSpinnerItemSelectedListener);

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
        mErrorText.setVisibility(View.GONE);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        GitLabClient.instance().getMergeRequests(mProject.getId(), mState).enqueue(mCallback);
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
