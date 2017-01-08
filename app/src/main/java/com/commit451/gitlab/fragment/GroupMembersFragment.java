package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alexgwyn.recyclerviewsquire.DynamicGridLayoutManager;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.GroupMembersAdapter;
import com.commit451.gitlab.dialog.AccessDialog;
import com.commit451.gitlab.event.MemberAddedEvent;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

public class GroupMembersFragment extends ButterKnifeFragment {

    private static final String KEY_GROUP = "group";

    public static GroupMembersFragment newInstance(Group group) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_GROUP, Parcels.wrap(group));

        GroupMembersFragment fragment = new GroupMembersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.root)
    View root;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView list;
    @BindView(R.id.message_text)
    TextView textMessage;
    @BindView(R.id.add_user_button)
    View buttonAddUser;

    GroupMembersAdapter adapterGroupMembers;
    DynamicGridLayoutManager layoutManagerGroupMembers;

    Member member;
    Group group;
    Uri nextPageUrl;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManagerGroupMembers.getChildCount();
            int totalItemCount = layoutManagerGroupMembers.getItemCount();
            int firstVisibleItem = layoutManagerGroupMembers.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !adapterGroupMembers.isLoading() && nextPageUrl != null) {
                loadMore();
            }
        }
    };

    private final GroupMembersAdapter.Listener listener = new GroupMembersAdapter.Listener() {
        @Override
        public void onUserClicked(Member member, ProjectMemberViewHolder holder) {
            Navigator.navigateToUser(getActivity(), holder.image, member);
        }

        @Override
        public void onUserRemoveClicked(Member member) {
            GroupMembersFragment.this.member = member;
            App.get().getGitLab().removeGroupMember(group.getId(), member.getId())
                    .compose(GroupMembersFragment.this.<String>bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CustomSingleObserver<String>() {

                        @Override
                        public void error(@NonNull Throwable e) {
                            Timber.e(e);
                            Snackbar.make(root, R.string.failed_to_remove_member, Snackbar.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void success(@NonNull String value) {
                            adapterGroupMembers.removeMember(GroupMembersFragment.this.member);
                        }
                    });
        }

        @Override
        public void onUserChangeAccessClicked(Member member) {
            AccessDialog accessDialog = new AccessDialog(getActivity(), member, group);
            accessDialog.setOnAccessChangedListener(new AccessDialog.OnAccessChangedListener() {
                @Override
                public void onAccessChanged(Member member, String accessLevel) {
                    loadData();
                }
            });
            accessDialog.show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        group = Parcels.unwrap(getArguments().getParcelable(KEY_GROUP));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_members, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App.bus().register(this);

        adapterGroupMembers = new GroupMembersAdapter(listener);
        layoutManagerGroupMembers = new DynamicGridLayoutManager(getActivity());
        layoutManagerGroupMembers.setMinimumWidthDimension(R.dimen.user_list_image_size);
        layoutManagerGroupMembers.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapterGroupMembers.isFooter(position)) {
                    return layoutManagerGroupMembers.getNumColumns();
                }
                return 1;
            }
        });
        list.setLayoutManager(layoutManagerGroupMembers);
        list.setAdapter(adapterGroupMembers);
        list.addOnScrollListener(mOnScrollListener);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        App.bus().unregister(this);
    }

    @OnClick(R.id.add_user_button)
    public void onAddUserClick(View fab) {
        Navigator.navigateToAddGroupMember(getActivity(), fab, group);
    }

    public void loadData() {
        if (getView() == null) {
            return;
        }
        if (group == null) {
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
        loadGroupMembers(App.get().getGitLab().getGroupMembers(group.getId()));
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

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

        adapterGroupMembers.setLoading(true);

        Timber.d("loadMore called for %s", nextPageUrl);
        loadGroupMembers(App.get().getGitLab().getProjectMembers(nextPageUrl.toString()));
    }

    private void loadGroupMembers(Single<Response<List<Member>>> observable) {
        observable
                .compose(this.<Response<List<Member>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Member>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.connection_error_users);
                        buttonAddUser.setVisibility(View.GONE);
                        adapterGroupMembers.setData(null);
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Member> members) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (members.isEmpty()) {
                            textMessage.setVisibility(View.VISIBLE);
                            textMessage.setText(R.string.no_project_members);
                        }
                        buttonAddUser.setVisibility(View.VISIBLE);
                        if (nextPageUrl == null) {
                            adapterGroupMembers.setData(members);
                        } else {
                            adapterGroupMembers.addData(members);
                        }
                        adapterGroupMembers.setLoading(false);

                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        Timber.d("Next page url %s", nextPageUrl);
                    }
                });
    }

    @Subscribe
    public void onMemberAdded(MemberAddedEvent event) {
        if (adapterGroupMembers != null) {
            adapterGroupMembers.addMember(event.member);
            textMessage.setVisibility(View.GONE);
        }
    }
}