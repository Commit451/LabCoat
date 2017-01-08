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

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.UserAdapter;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.gitlab.viewHolder.UserViewHolder;

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
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listUsers;
    @BindView(R.id.message_text)
    TextView textMessage;

    UserAdapter adapterUser;
    GridLayoutManager layoutManagerUser;

    String query;
    boolean loading;
    Uri nextPageUrl;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManagerUser.getChildCount();
            int totalItemCount = layoutManagerUser.getItemCount();
            int firstVisibleItem = layoutManagerUser.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        query = getArguments().getString(EXTRA_QUERY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_projects, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapterUser = new UserAdapter(new UserAdapter.Listener() {
            @Override
            public void onUserClicked(UserBasic user, UserViewHolder userViewHolder) {
                Navigator.navigateToUser(getActivity(), userViewHolder.image, user);
            }
        });
        layoutManagerUser = new GridLayoutManager(getActivity(), 2);
        layoutManagerUser.setSpanSizeLookup(adapterUser.getSpanSizeLookup());
        listUsers.setLayoutManager(layoutManagerUser);
        listUsers.setAdapter(adapterUser);
        listUsers.addOnScrollListener(onScrollListener);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        loadData();
    }

    @Override
    protected void loadData() {
        loading = true;
        if (getView() == null) {
            return;
        }

        if (TextUtils.isEmpty(query)) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        textMessage.setVisibility(View.GONE);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        App.get().getGitLab().searchUsers(query)
                .compose(this.<Response<List<UserBasic>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<UserBasic>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        loading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setText(R.string.connection_error_users);
                        textMessage.setVisibility(View.VISIBLE);
                        adapterUser.setData(null);
                    }

                    @Override
                    public void responseSuccess(@NonNull List<UserBasic> users) {
                        swipeRefreshLayout.setRefreshing(false);
                        loading = false;
                        if (users.isEmpty()) {
                            textMessage.setVisibility(View.VISIBLE);
                            textMessage.setText(R.string.no_users_found);
                        }
                        adapterUser.setData(users);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                    }
                });
    }

    private void loadMore() {
        loading = true;
        adapterUser.setLoading(true);
        Timber.d("loadMore called for %s %s", nextPageUrl.toString(), query);
        App.get().getGitLab().searchUsers(nextPageUrl.toString(), query)
                .compose(this.<Response<List<UserBasic>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<UserBasic>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        loading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        adapterUser.setLoading(false);
                    }

                    @Override
                    public void responseSuccess(@NonNull List<UserBasic> users) {
                        loading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        adapterUser.addData(users);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        adapterUser.setLoading(false);
                    }
                });
    }

    public void searchQuery(String query) {
        this.query = query;

        if (adapterUser != null) {
            adapterUser.clearData();
            loadData();
        }
    }
}
