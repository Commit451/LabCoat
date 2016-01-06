package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
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
import com.commit451.gitlab.adapter.MilestoneAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.MilestoneChangedEvent;
import com.commit451.gitlab.event.MilestoneCreatedEvent;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.NavigationManager;
import com.commit451.gitlab.util.PaginationUtil;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class MilestonesFragment extends BaseFragment {

    public static MilestonesFragment newInstance() {
        return new MilestonesFragment();
    }

    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mRecyclerView;
    @Bind(R.id.message_text) TextView mMessageView;
    @Bind(R.id.state_spinner) Spinner mSpinner;

    private Project mProject;
    private EventReceiver mEventReceiver;
    private MilestoneAdapter mMilestoneAdapter;
    private LinearLayoutManager mMilestoneLayoutManager;

    @BindString(R.string.merge_request_state_value_default)
    String mState;
    private String[] mStates;
    private boolean mLoading = false;
    private Uri mNextPageUrl;

    @OnClick(R.id.add)
    public void onAddClicked(View fab) {
        if (mProject != null) {
            NavigationManager.navigateToAddMilestone(getActivity(), fab, mProject);
        } else {
            Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private final AdapterView.OnItemSelectedListener mSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mState = mStates[position];
            loadData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private final MilestoneAdapter.Listener mMilestoneListener = new MilestoneAdapter.Listener() {
        @Override
        public void onMilestoneClicked(Milestone milestone) {
            NavigationManager.navigateToMilestone(getActivity(), mProject, milestone);
        }
    };

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mMilestoneLayoutManager.getChildCount();
            int totalItemCount = mMilestoneLayoutManager.getItemCount();
            int firstVisibleItem = mMilestoneLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMore();
            }
        }
    };

    private final Callback<List<Milestone>> mCallback = new Callback<List<Milestone>>() {
        @Override
        public void onResponse(Response<List<Milestone>> response, Retrofit retrofit) {
            mLoading = false;
            if (getView() == null) {
                return;
            }

            if (!response.isSuccess()) {
                Timber.e("Milestones requests response was not a success: %d", response.code());
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.connection_error_merge_requests);
                mMilestoneAdapter.setData(null);
                mNextPageUrl = null;
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);

            if (!response.body().isEmpty()) {
                mMessageView.setVisibility(View.GONE);
            } else if (mNextPageUrl == null) {
                Timber.d("No milestones requests found");
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_milestones);
            }

            if (mNextPageUrl == null) {
                mMilestoneAdapter.setData(response.body());
            } else {
                mMilestoneAdapter.addData(response.body());
            }

            mNextPageUrl = PaginationUtil.parse(response).getNext();
            Timber.d("Next page url " + mNextPageUrl);
        }

        @Override
        public void onFailure(Throwable t) {
            mLoading = false;
            Timber.e(t, null);

            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);

            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.connection_error);
            mMilestoneAdapter.setData(null);
            mNextPageUrl = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStates = getContext().getResources().getStringArray(R.array.merge_request_state_values);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_milestones, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mEventReceiver = new EventReceiver();
        GitLabApp.bus().register(mEventReceiver);

        mMilestoneAdapter = new MilestoneAdapter(mMilestoneListener);
        mMilestoneLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mMilestoneLayoutManager);
        mRecyclerView.setAdapter(mMilestoneAdapter);
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
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

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

        GitLabClient.instance().getMilestones(mProject.getId()).enqueue(mCallback);
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

        if (mNextPageUrl == null) {
            return;
        }

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        mLoading = true;

        Timber.d("loadMore called for " + mNextPageUrl);
        GitLabClient.instance().getMilestones(mNextPageUrl.toString()).enqueue(mCallback);
    }

    private class EventReceiver {
        @Subscribe
        public void onProjectReload(ProjectReloadEvent event) {
            mProject = event.mProject;
            loadData();
        }

        @Subscribe
        public void onMilestoneCreated(MilestoneCreatedEvent event) {
            mMilestoneAdapter.addMilestone(event.mMilestone);
            if (getView() != null) {
                mMessageView.setVisibility(View.GONE);
                mRecyclerView.smoothScrollToPosition(0);
            }
        }

        @Subscribe
        public void onIssueChanged(MilestoneChangedEvent event) {
            mMilestoneAdapter.updateIssue(event.mMilestone);
        }
    }
}
