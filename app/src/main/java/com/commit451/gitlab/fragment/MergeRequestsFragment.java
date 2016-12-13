package com.commit451.gitlab.fragment;

import android.net.Uri;
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

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.adapter.MergeRequestAdapter;
import com.commit451.gitlab.event.MergeRequestChangedEvent;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.reptar.retrofit.ResponseSingleObserver;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

public class MergeRequestsFragment extends ButterKnifeFragment {

    public static MergeRequestsFragment newInstance() {
        return new MergeRequestsFragment();
    }

    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list) RecyclerView mRecyclerView;
    @BindView(R.id.message_text) TextView mMessageView;
    @BindView(R.id.state_spinner) Spinner mSpinner;

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
            Navigator.navigateToMergeRequest(getActivity(), mProject, mergeRequest);
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

        mEventReceiver = new EventReceiver();
        App.bus().register(mEventReceiver);

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
        App.bus().unregister(mEventReceiver);
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
        App.get().getGitLab().getMergeRequests(mProject.getId(), mState)
                .compose(this.<Response<List<MergeRequest>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseSingleObserver<List<MergeRequest>>() {

                    @Override
                    public void onError(Throwable e) {
                        mLoading = false;
                        Timber.e(e);
                        mSwipeRefreshLayout.setRefreshing(false);
                        mMessageView.setVisibility(View.VISIBLE);
                        mMessageView.setText(R.string.connection_error_merge_requests);
                        mMergeRequestAdapter.setData(null);
                        mNextPageUrl = null;
                    }

                    @Override
                    protected void onResponseSuccess(List<MergeRequest> mergeRequests) {
                        mLoading = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (mergeRequests.isEmpty()) {
                            mMessageView.setVisibility(View.VISIBLE);
                            mMessageView.setText(R.string.no_merge_requests);
                        }
                        mMergeRequestAdapter.setData(mergeRequests);
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        Timber.d("Next page url " + mNextPageUrl);
                    }
                });
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
        App.get().getGitLab().getMergeRequests(mNextPageUrl.toString(), mState)
                .compose(this.<Response<List<MergeRequest>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseSingleObserver<List<MergeRequest>>() {

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mMergeRequestAdapter.setLoading(false);
                        mLoading = false;
                    }

                    @Override
                    protected void onResponseSuccess(List<MergeRequest> mergeRequests) {
                        mLoading = false;
                        mMergeRequestAdapter.setLoading(false);
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        mMergeRequestAdapter.addData(mergeRequests);
                    }
                });
    }

    private class EventReceiver {
        @Subscribe
        public void onProjectReload(ProjectReloadEvent event) {
            mProject = event.mProject;
            loadData();
        }

        @Subscribe
        public void onMergeRequestChanged(MergeRequestChangedEvent event) {
            loadData();
        }
    }
}
