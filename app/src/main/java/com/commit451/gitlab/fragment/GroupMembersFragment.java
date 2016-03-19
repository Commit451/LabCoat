package com.commit451.gitlab.fragment;

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

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.GroupMembersAdapter;
import com.commit451.gitlab.api.EasyCallback;
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
import timber.log.Timber;

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
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mRecyclerView;
    @Bind(R.id.message_text) TextView mMessageView;
    @Bind(R.id.add_user_button) View mAddUserButton;

    private Group mGroup;
    private EventReceiver mEventReceiver;
    private GroupMembersAdapter mGroupMembersAdapter;
    private Member mMember;

    private final AccessDialog.OnAccessChangedListener mOnAccessChangedListener = new AccessDialog.OnAccessChangedListener() {
        @Override
        public void onAccessChanged(Member member, String accessLevel) {
            loadData();
        }
    };

    private final EasyCallback<List<Member>> mGroupMembersCallback = new EasyCallback<List<Member>>() {
        @Override
        public void onResponse(@NonNull List<Member> response) {
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (response.isEmpty()) {
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_project_members);
            }
            mAddUserButton.setVisibility(View.VISIBLE);
            mGroupMembersAdapter.setData(response);
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.connection_error_users);
            mAddUserButton.setVisibility(View.GONE);
            mGroupMembersAdapter.setData(null);
        }
    };

    private final EasyCallback<Void> mRemoveMemberCallback = new EasyCallback<Void>() {
        @Override
        public void onResponse(@NonNull Void response) {
            if (getView() == null) {
                return;
            }
            mGroupMembersAdapter.removeMember(mMember);
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            if (getView() == null) {
                return;
            }
            Snackbar.make(mRoot, R.string.failed_to_remove_member, Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private final GroupMembersAdapter.Listener mListener = new GroupMembersAdapter.Listener() {
        @Override
        public void onUserClicked(Member member, ProjectMemberViewHolder holder) {
            NavigationManager.navigateToUser(getActivity(), holder.mImageView, member);
        }

        @Override
        public void onUserRemoveClicked(Member member) {
            mMember = member;
            GitLabClient.instance().removeGroupMember(mGroup.getId(), member.getId()).enqueue(mRemoveMemberCallback);
        }

        @Override
        public void onUserChangeAccessClicked(Member member) {
            AccessDialog accessDialog = new AccessDialog(getActivity(), member, mGroup);
            accessDialog.setOnAccessChangedListener(mOnAccessChangedListener);
            accessDialog.show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGroup = Parcels.unwrap(getArguments().getParcelable(KEY_GROUP));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_members, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mEventReceiver = new EventReceiver();
        LabCoatApp.bus().register(mEventReceiver);

        mGroupMembersAdapter = new GroupMembersAdapter(mListener);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setAdapter(mGroupMembersAdapter);

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
        LabCoatApp.bus().unregister(mEventReceiver);
    }

    @OnClick(R.id.add_user_button)
    public void onAddUserClick(View fab) {
        NavigationManager.navigateToAddGroupMember(getActivity(), fab, mGroup);
    }

    public void loadData() {
        if (getView() == null) {
            return;
        }
        if (mGroup == null) {
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
        GitLabClient.instance().getGroupMembers(mGroup.getId()).enqueue(mGroupMembersCallback);
    }

    private class EventReceiver {
        @Subscribe
        public void onMemberAdded(MemberAddedEvent event) {
            if (mGroupMembersAdapter != null) {
                mGroupMembersAdapter.addMember(event.mMember);
                mMessageView.setVisibility(View.GONE);
            }
        }
    }
}