package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.UsersAdapter;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.gitlab.viewHolder.UserViewHolder;
import com.commit451.reptar.retrofit.ResponseSingleObserver;

import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
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

        App.get().getGitLab().searchUsers(mQuery)
                .compose(this.<Response<List<UserBasic>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseSingleObserver<List<UserBasic>>() {

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mLoading = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                        mMessageView.setText(R.string.connection_error_users);
                        mMessageView.setVisibility(View.VISIBLE);
                        mUsersAdapter.setData(null);
                    }

                    @Override
                    protected void onResponseSuccess(List<UserBasic> users) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mLoading = false;
                        if (users.isEmpty()) {
                            mMessageView.setVisibility(View.VISIBLE);
                            mMessageView.setText(R.string.no_users_found);
                        }
                        mUsersAdapter.setData(users);
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                    }
                });
    }

    private void loadMore() {
        mLoading = true;
        mUsersAdapter.setLoading(true);
        Timber.d("loadMore called for %s %s", mNextPageUrl.toString(), mQuery);
        App.get().getGitLab().searchUsers(mNextPageUrl.toString(), mQuery)
                .compose(this.<Response<List<UserBasic>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseSingleObserver<List<UserBasic>>() {

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mLoading = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                        mUsersAdapter.setLoading(false);
                    }

                    @Override
                    protected void onResponseSuccess(List<UserBasic> users) {
                        mLoading = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                        mUsersAdapter.addData(users);
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        mUsersAdapter.setLoading(false);
                    }
                });
    }

    public void searchQuery(String query) {
        mQuery = query;

        if (mUsersAdapter != null) {
            mUsersAdapter.clearData();
            loadData();
        }
    }
}
