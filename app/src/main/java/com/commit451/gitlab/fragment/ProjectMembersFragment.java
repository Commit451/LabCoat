package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.ProjectMembersAdapter;
import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.dialog.AccessDialog;
import com.commit451.gitlab.event.MemberAddedEvent;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class ProjectMembersFragment extends ButterKnifeFragment {

    public static ProjectMembersFragment newInstance() {
        return new ProjectMembersFragment();
    }

    @BindView(R.id.root) View mRoot;
    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list) RecyclerView mMembersListView;
    @BindView(R.id.message_text) TextView mMessageView;
    @BindView(R.id.add_user_button) FloatingActionButton mAddUserButton;

    Project mProject;
    EventReceiver mEventReceiver;
    ProjectMembersAdapter mAdapter;
    GridLayoutManager mProjectLayoutManager;
    Member mMember;
    Uri mNextPageUrl;
    boolean mLoading = false;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mProjectLayoutManager.getChildCount();
            int totalItemCount = mProjectLayoutManager.getItemCount();
            int firstVisibleItem = mProjectLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMore();
            }
        }
    };

    private final AccessDialog.OnAccessChangedListener mOnAccessChangedListener = new AccessDialog.OnAccessChangedListener() {
        @Override
        public void onAccessChanged(Member member, String accessLevel) {
            loadData();
        }
    };

    private final ProjectMembersAdapter.Listener mMemberAdapterListener = new ProjectMembersAdapter.Listener() {
        @Override
        public void onProjectMemberClicked(Member member, ProjectMemberViewHolder memberGroupViewHolder) {
            Navigator.navigateToUser(getActivity(), memberGroupViewHolder.mImageView, member);
        }

        @Override
        public void onRemoveMember(Member member) {
            mMember = member;
            App.get().getGitLab().removeProjectMember(mProject.getId(), member.getId()).enqueue(mRemoveMemberCallback);
        }

        @Override
        public void onChangeAccess(Member member) {
            AccessDialog accessDialog = new AccessDialog(getActivity(), member, mProject.getId());
            accessDialog.setOnAccessChangedListener(mOnAccessChangedListener);
            accessDialog.show();
        }

        @Override
        public void onSeeGroupClicked() {
            Navigator.navigateToGroup(getActivity(), mProject.getNamespace().getId());
        }
    };

    private final EasyCallback<List<Member>> mProjectMembersCallback = new EasyCallback<List<Member>>() {
        @Override
        public void success(@NonNull List<Member> response) {
            mLoading = false;
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (!response.isEmpty()) {
                mMessageView.setVisibility(View.GONE);
            } else if (mNextPageUrl == null) {
                Timber.d("No project members found");
                mMessageView.setText(R.string.no_project_members);
                mMessageView.setVisibility(View.VISIBLE);
            }

            mAddUserButton.setVisibility(View.VISIBLE);

            if (mNextPageUrl == null) {
                mAdapter.setProjectMembers(response);
            } else {
                mAdapter.addProjectMembers(response);
            }

            mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
            Timber.d("Next page url " + mNextPageUrl);
        }

        @Override
        public void failure(Throwable t) {
            mLoading = false;
            Timber.e(t);
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.connection_error_users);
            mAddUserButton.setVisibility(View.GONE);
            mAdapter.setProjectMembers(null);
            mNextPageUrl = null;
        }
    };

    private final EasyCallback<Void> mRemoveMemberCallback = new EasyCallback<Void>() {
        @Override
        public void success(@NonNull Void response) {
            if (getView() == null) {
                return;
            }
            mAdapter.removeMember(mMember);
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t);
            if (getView() == null) {
                return;
            }
            Snackbar.make(mRoot, R.string.failed_to_remove_member, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }.allowNullBodies(true);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_members, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEventReceiver = new EventReceiver();
        App.bus().register(mEventReceiver);

        mAdapter = new ProjectMembersAdapter(mMemberAdapterListener);
        mProjectLayoutManager = new GridLayoutManager(getActivity(), 2);
        mProjectLayoutManager.setSpanSizeLookup(mAdapter.getSpanSizeLookup());
        mMembersListView.setLayoutManager(mProjectLayoutManager);
        mMembersListView.setAdapter(mAdapter);
        mMembersListView.addOnScrollListener(mOnScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        if (getActivity() instanceof ProjectActivity) {
            mProject = ((ProjectActivity) getActivity()).getProject();
            setNamespace();
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

    @OnClick(R.id.add_user_button)
    public void onAddUserClick(View fab) {
        Navigator.navigateToAddProjectMember(getActivity(), fab, mProject.getId());
    }

    @Override
    public void loadData() {
        if (getView() == null) {
            return;
        }

        if (mProject == null) {
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

        mNextPageUrl = null;
        mLoading = true;

        App.get().getGitLab().getProjectMembers(mProject.getId()).enqueue(mProjectMembersCallback);
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

        if (mNextPageUrl == null) {
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

        mLoading = true;

        Timber.d("loadMore called for " + mNextPageUrl);
        App.get().getGitLab().getProjectMembers(mNextPageUrl.toString()).enqueue(mProjectMembersCallback);
    }

    private void setNamespace() {
        if (mProject == null) {
            return;
        }

        //If there is an owner, then there is no group
        if (mProject.belongsToGroup()) {
            mAdapter.setNamespace(mProject.getNamespace());
        } else {
            mAdapter.setNamespace(null);
        }
    }

    private class EventReceiver {
        @Subscribe
        public void onProjectReload(ProjectReloadEvent event) {
            mProject = event.mProject;
            setNamespace();
            loadData();
        }

        @Subscribe
        public void onMemberAdded(MemberAddedEvent event) {
            if (mAdapter != null) {
                mAdapter.addMember(event.mMember);

                if (getView() != null) {
                    mMessageView.setVisibility(View.GONE);
                }
            }
        }
    }
}