package com.commit451.gitlab.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.GroupMembersAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.dialog.AccessDialog;
import com.commit451.gitlab.event.MemberAddedEvent;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.util.NavigationManager;
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder;
import com.squareup.otto.Subscribe;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Fragment to show the members of a {@link Group}
 */
public class GroupMembersFragment extends BaseFragment {

    private static final String KEY_GROUP = "group";

    public static GroupMembersFragment newInstance(Group group) {

        Bundle args = new Bundle();
        args.putParcelable(KEY_GROUP, Parcels.wrap(group));

        GroupMembersFragment fragment = new GroupMembersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.root) View mRoot;
    @Bind(R.id.add_user_button) View mAddUserButton;
    @Bind(R.id.list) RecyclerView mRecyclerView;
    GroupMembersAdapter mAdapter;
    @Bind(R.id.error_text) TextView mErrorText;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;

    @OnClick(R.id.add_user_button)
    public void onAddUserClick() {
        NavigationManager.navigateToAddGroupMember(getActivity(), mGroup);
    }

    Group mGroup;
    EventReceiver mEventReceiver;
    Member mMember;

    private final Callback<List<Member>> mGroupMembersCallback = new Callback<List<Member>>() {

        @Override
        public void onResponse(Response<List<Member>> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (!response.isSuccess()) {
                mErrorText.setText(R.string.connection_error);
                mErrorText.setVisibility(View.VISIBLE);
                return;
            }
            if (response.body().isEmpty()) {
                mErrorText.setText(R.string.no_project_members);
                mErrorText.setVisibility(View.VISIBLE);
            } else {
                mErrorText.setVisibility(View.GONE);
            }
            mAddUserButton.setVisibility(View.VISIBLE);

            mAdapter.setData(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mErrorText.setText(R.string.connection_error);
            mErrorText.setVisibility(View.VISIBLE);
            mAddUserButton.setVisibility(View.GONE);
            Snackbar.make(mRoot, getString(R.string.connection_error_users), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private final Callback<Void> mRemoveUserCallback = new Callback<Void>() {
        @Override
        public void onResponse(Response<Void> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }
            if (!response.isSuccess()) {
                Snackbar.make(mRoot, R.string.failed_to_remove_member, Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            mAdapter.removeMember(mMember);
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(null, t);
            Snackbar.make(mRoot, R.string.connection_error, Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private final SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            loadData();
        }
    };

    private AccessDialog.OnAccessChangedListener mOnAccessChangedListener = new AccessDialog.OnAccessChangedListener() {
        @Override
        public void onAccessChanged(Member member, String accessLevel) {
            loadData();
        }
    };

    private final GroupMembersAdapter.Listener mListener = new GroupMembersAdapter.Listener() {
        @Override
        public void onUserClicked(Member member, ProjectMemberViewHolder holder) {
            NavigationManager.navigateToUser(getActivity(), holder.image, member);
        }

        @Override
        public void onUserRemoveClicked(Member member) {
            mMember = member;
            GitLabClient.instance().removeGroupMember(mGroup.getId(), member.getId()).enqueue(mRemoveUserCallback);
        }

        @Override
        public void onUserChangeAccessClicked(Member member) {
            AccessDialog accessDialog = new AccessDialog(getActivity(), member, mGroup);
            accessDialog.setOnAccessChangedListener(mOnAccessChangedListener);
            accessDialog.show();
        }
    };

    public GroupMembersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_members, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mGroup = Parcels.unwrap(getArguments().getParcelable(KEY_GROUP));

        mAdapter = new GroupMembersAdapter(mListener);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);

        mEventReceiver = new EventReceiver();
        GitLabApp.bus().register(mEventReceiver);
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        GitLabApp.bus().unregister(mEventReceiver);
    }

    public void loadData() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        GitLabClient.instance().getGroupMembers(mGroup.getId()).enqueue(mGroupMembersCallback);
    }

    private class EventReceiver {

        @Subscribe
        public void onUserAdded(MemberAddedEvent event) {
            if (mAdapter != null) {
                mAdapter.addMember(event.member);
            }
        }
    }
}