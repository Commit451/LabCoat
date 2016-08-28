package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.UsersAdapter;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.util.PaginationUtil;
import com.commit451.gitlab.viewHolder.UserViewHolder;

import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

public class UsersFragment extends ButterKnifeFragment {

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

    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView mUsersListView;
    @BindView(R.id.message_text)
    TextView mMessageView;
    private GridLayoutManager mUserLinearLayoutManager;

    private String mQuery;
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
            Navigator.navigateToUser(getActivity(), userViewHolder.mImageView, user);
        }
    };

    public EasyCallback<List<UserBasic>> mSearchCallback = new EasyCallback<List<UserBasic>>() {
        @Override
        public void success(@NonNull List<UserBasic> response) {
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mLoading = false;
            if (response.isEmpty()) {
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_users_found);
            }
            mUsersAdapter.setData(response);
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mLoading = false;
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mMessageView.setText(R.string.connection_error_users);
            mMessageView.setVisibility(View.VISIBLE);
            mUsersAdapter.setData(null);
        }
    };

    public EasyCallback<List<UserBasic>> mMoreUsersCallback = new EasyCallback<List<UserBasic>>() {
        @Override
        public void success(@NonNull List<UserBasic> response) {
            mLoading = false;
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mUsersAdapter.addData(response);
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
            mUsersAdapter.setLoading(false);
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mLoading = false;
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mUsersAdapter.setLoading(false);
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

        mUsersAdapter = new UsersAdapter(mUsersAdapterListener);
        mUserLinearLayoutManager = new GridLayoutManager(getActivity(), 2);
        mUserLinearLayoutManager.setSpanSizeLookup(mUsersAdapter.getSpanSizeLookup());
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
    protected void loadData() {
        mLoading = true;
        if (getView() == null) {
            return;
        }

        if (TextUtils.isEmpty(mQuery)) {
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

        App.instance().getGitLab().searchUsers(mQuery).enqueue(mSearchCallback);
    }

    private void loadMore() {
        mLoading = true;
        mUsersAdapter.setLoading(true);
        Timber.d("loadMore called for %s %s", mNextPageUrl.toString(), mQuery);
        App.instance().getGitLab().searchUsers(mNextPageUrl.toString(), mQuery).enqueue(mMoreUsersCallback);
    }

    public void searchQuery(String query) {
        mQuery = query;

        if (mUsersAdapter != null) {
            mUsersAdapter.clearData();
            loadData();
        }
    }
}
