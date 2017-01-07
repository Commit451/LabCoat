package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.GroupAdapter;
import com.commit451.gitlab.event.CloseDrawerEvent;
import com.commit451.gitlab.event.ReloadDataEvent;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.util.DynamicGridLayoutManager;
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.gitlab.viewHolder.GroupViewHolder;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Displays the groups of the current user
 */
public class GroupsActivity extends BaseActivity {

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, GroupsActivity.class);
        return intent;
    }

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listGroups;
    @BindView(R.id.message_text)
    TextView textMessage;

    GroupAdapter adapterGroup;
    DynamicGridLayoutManager layoutManager;

    private Uri nextPageUrl;
    private boolean loading = false;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore();
            }
        }
    };

    private final GroupAdapter.Listener groupAdapterListener = new GroupAdapter.Listener() {
        @Override
        public void onGroupClicked(Group group, GroupViewHolder groupViewHolder) {
            Navigator.navigateToGroup(GroupsActivity.this, groupViewHolder.mImageView, group);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ButterKnife.bind(this);
        App.bus().register(this);

        toolbar.setTitle(R.string.nav_groups);
        toolbar.setNavigationIcon(R.drawable.ic_menu_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });
        textMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load();
            }
        });
        layoutManager = new DynamicGridLayoutManager(this);
        layoutManager.setMinimumWidthDimension(R.dimen.user_list_image_size);
        listGroups.setLayoutManager(layoutManager);
        adapterGroup = new GroupAdapter(groupAdapterListener);
        listGroups.setAdapter(adapterGroup);
        listGroups.addOnScrollListener(onScrollListener);
        load();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.bus().unregister(this);
    }

    private void load() {
        textMessage.setVisibility(View.GONE);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        nextPageUrl = null;
        loading = true;

        App.get().getGitLab().getGroups()
                .compose(this.<Response<List<Group>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Group>>() {

                    @Override
                    public void error(Throwable e) {
                        Timber.e(e);
                        swipeRefreshLayout.setRefreshing(false);
                        loading = false;
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.connection_error);
                    }

                    @Override
                    public void responseSuccess(List<Group> groups) {
                        loading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        if (groups.isEmpty()) {
                            textMessage.setText(R.string.no_groups);
                            textMessage.setVisibility(View.VISIBLE);
                            listGroups.setVisibility(View.GONE);
                        } else {
                            adapterGroup.setGroups(groups);
                            textMessage.setVisibility(View.GONE);
                            listGroups.setVisibility(View.VISIBLE);
                            nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        }
                    }
                });
    }

    private void loadMore() {
        if (nextPageUrl == null) {
            return;
        }

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        loading = true;

        Timber.d("loadMore called for %s", nextPageUrl);
        App.get().getGitLab().getGroups(nextPageUrl.toString())
                .compose(this.<Response<List<Group>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Group>>() {

                    @Override
                    public void error(Throwable e) {
                        Timber.e(e);
                        loading = false;
                    }

                    @Override
                    public void responseSuccess(List<Group> groups) {
                        loading = false;
                        adapterGroup.addGroups(groups);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                    }
                });
    }

    @Subscribe
    public void onCloseDrawerEvent(CloseDrawerEvent event) {
        drawerLayout.closeDrawers();
    }

    @Subscribe
    public void onReloadData(ReloadDataEvent event) {
        load();
    }

}
