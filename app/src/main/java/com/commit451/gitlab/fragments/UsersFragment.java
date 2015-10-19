package com.commit451.gitlab.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.UsersAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.NavigationManager;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by John on 9/28/15.
 */
public class UsersFragment extends BaseFragment {

    private static final String EXTRA_QUERY = "extra_query";

    public static UsersFragment newInstance() {
        return newInstance(null);
    }

    public static UsersFragment newInstance(String query) {
        Bundle args = new Bundle();
        if (query != null) {
            args.putString(EXTRA_QUERY, query);
        }
        UsersFragment fragment = new UsersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mUsersList;
    UsersAdapter mUsersAdapter;
    @Bind(R.id.message_text) TextView mMessageText;
    String mQuery;

    private final SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            loadData();
        }
    };

    private final UsersAdapter.Listener mUsersAdapterListener = new UsersAdapter.Listener() {
        @Override
        public void onUserClicked(User user) {
            NavigationManager.navigateToUser(getActivity(), user);
        }
    };

    public Callback<List<User>> mSearchCallback = new Callback<List<User>>() {

        @Override
        public void onResponse(Response<List<User>> response, Retrofit retrofit) {
            mSwipeRefreshLayout.setRefreshing(false);
            if (!response.isSuccess()) {
                return;
            }
            if (getView() == null) {
                return;
            }
            if (response.body().size() == 0) {
                mMessageText.setText(R.string.no_users_found);
                mMessageText.setVisibility(View.VISIBLE);
                mUsersList.setVisibility(View.GONE);
            } else {
                mMessageText.setVisibility(View.GONE);
                mUsersList.setVisibility(View.VISIBLE);
                mUsersAdapter.setData(response.body());
            }

        }

        @Override
        public void onFailure(Throwable t) {
            if (getView() == null) {
                return;
            }
            mMessageText.setVisibility(View.VISIBLE);
            Timber.e(t.toString());
            Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_users), Snackbar.LENGTH_SHORT)
                    .show();
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
        mUsersAdapter = new UsersAdapter(mUsersAdapterListener);
        mUsersList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUsersList.setAdapter(mUsersAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        if (!TextUtils.isEmpty(mQuery)) {
            loadData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    protected void loadData() {
        super.loadData();
        mMessageText.setVisibility(View.GONE);
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

    public void searchQuery(String query) {
        mUsersAdapter.clearData();
        mQuery = query;
        loadData();
    }
}
