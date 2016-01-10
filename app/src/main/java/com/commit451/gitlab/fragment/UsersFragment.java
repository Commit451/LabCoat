package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.UsersAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.util.NavigationManager;
import com.commit451.gitlab.util.PaginationUtil;
import com.commit451.gitlab.viewHolder.UserViewHolder;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class UsersFragment extends BaseFragment {

    private static final String EXTRA_QUERY = "extra_query";

    public static UsersFragment newInstance() {
        return newInstance(null);
    }

    public static UsersFragment newInstance(String query) {
        Bundle args = new Bundle();
        if (query != null) {
            args.putString(EXTRA_QUERY, query);
        } else {
            args.putString(EXTRA_QUERY, "");
        }

        UsersFragment fragment = new UsersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mUsersListView;
    @Bind(R.id.message_text) TextView mMessageView;
    private LinearLayoutManager mUserLinearLayoutManager;

    private String mQuery;
    private EventReceiver mEventReceiver;
    private UsersAdapter mUsersAdapter;
    private boolean mLoading;
    private Uri mNextPageUrl;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mUserLinearLayoutManager.getChildCount();
            int totalItemCount = mUserLinearLayoutManager.getItemCount();
            int firstVisibleItem = mUserLinearLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMore();
            }
        }
    };

    private final UsersAdapter.Listener mUsersAdapterListener = new UsersAdapter.Listener() {
        @Override
        public void onUserClicked(UserBasic user, UserViewHolder userViewHolder) {
            NavigationManager.navigateToUser(getActivity(), userViewHolder.mImageView, user);
        }
    };

    public Callback<List<UserBasic>> mSearchCallback = new Callback<List<UserBasic>>() {
        @Override
        public void onResponse(Response<List<UserBasic>> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mLoading = false;

            if (!response.isSuccess()) {
                Timber.e("Users response was not a success: %d", response.code());
                mMessageView.setText(R.string.connection_error_users);
                mMessageView.setVisibility(View.VISIBLE);
                mUsersAdapter.setData(null);
                return;
            }

            if (!response.body().isEmpty()) {
                mMessageView.setVisibility(View.GONE);
            } else {
                Timber.d("No users found");
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_users_found);
            }
            mUsersAdapter.setData(response.body());
            mNextPageUrl = PaginationUtil.parse(response).getNext();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mLoading = false;

            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);

            mMessageView.setText(R.string.connection_error);
            mMessageView.setVisibility(View.VISIBLE);
            mUsersAdapter.setData(null);
        }
    };

    public Callback<List<UserBasic>> mMoreUsersCallback = new Callback<List<UserBasic>>() {
        @Override
        public void onResponse(Response<List<UserBasic>> response, Retrofit retrofit) {
            mLoading = false;
            if (getView() == null || !response.isSuccess()) {
                return;
            }
            mUsersAdapter.addData(response.body());
            mNextPageUrl = PaginationUtil.parse(response).getNext();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mLoading = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        mEventReceiver = new EventReceiver();
        GitLabApp.bus().register(mEventReceiver);

        mUsersAdapter = new UsersAdapter(mUsersAdapterListener);
        mUserLinearLayoutManager = new GridLayoutManager(getActivity(), 2);
        mUsersListView.setLayoutManager(mUserLinearLayoutManager);
        mUsersListView.setAdapter(mUsersAdapter);
        mUsersListView.addOnScrollListener(mOnScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        GitLabApp.bus().unregister(mEventReceiver);
    }

    @Override
    protected void loadData() {
        mLoading = true;
        if (getView() == null) {
            return;
        }

        if (TextUtils.isEmpty(mQuery)) {
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

        GitLabClient.instance().searchUsers(mQuery).enqueue(mSearchCallback);
    }

    private void loadMore() {
        mLoading = true;
        Timber.d("loadMore called for " + mNextPageUrl.toString() + " " + mQuery);
        GitLabClient.instance().searchUsers(mNextPageUrl.toString(), mQuery).enqueue(mMoreUsersCallback);
    }

    public void searchQuery(String query) {
        mUsersAdapter.clearData();
        mQuery = query;
        loadData();
    }

    private class EventReceiver {
    }
}
