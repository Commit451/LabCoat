package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.MemberAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.dialog.AccessDialog;
import com.commit451.gitlab.event.MemberAddedEvent;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.NavigationManager;
import com.commit451.gitlab.util.PaginationUtil;
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class ProjectMembersFragment extends BaseFragment {

    public static ProjectMembersFragment newInstance() {
        return new ProjectMembersFragment();
    }

    @Bind(R.id.root) View mRoot;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mMembersListView;
    @Bind(R.id.message_text) TextView mMessageView;
    @Bind(R.id.add_user_button) FloatingActionButton mAddUserButton;

    private Project mProject;
    private EventReceiver mEventReceiver;
    private MemberAdapter mAdapter;
    private GridLayoutManager mProjectLayoutManager;
    private Member mMember;
    private Uri mNextPageUrl;
    private boolean mLoading = false;

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

    private final Callback<List<Member>> mProjectMembersCallback = new Callback<List<Member>>() {
        @Override
        public void onResponse(Response<List<Member>> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mLoading = false;

            if (!response.isSuccess()) {
                Timber.e("Project members response was not a success: %d", response.code());
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.connection_error_users);
                mAddUserButton.setVisibility(View.GONE);
                return;
            }

            if (!response.body().isEmpty()) {
                mMessageView.setVisibility(View.GONE);
            } else {
                Timber.d("No project members found");
                mMessageView.setText(R.string.no_project_members);
                mMessageView.setVisibility(View.VISIBLE);
            }

            mAddUserButton.setVisibility(View.VISIBLE);

            mAdapter.setProjectMembers(response.body());
            mNextPageUrl = PaginationUtil.parse(response).getNext();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);

            if (getView() == null) {
                return;
            }

            mLoading = false;
            mSwipeRefreshLayout.setRefreshing(false);

            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.connection_error);
            mAddUserButton.setVisibility(View.GONE);
        }
    };

    private final Callback<List<Member>> mMoreProjectMembersCallback = new Callback<List<Member>>() {
        @Override
        public void onResponse(Response<List<Member>> response, Retrofit retrofit) {

            mLoading = false;
            if (getView() == null || !response.isSuccess()) {
                return;
            }

            mAdapter.addProjectMembers(response.body());
            mNextPageUrl = PaginationUtil.parse(response).getNext();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);

            if (getView() == null) {
                return;
            }
            mLoading = false;
        }
    };

    private final Callback<Void> mRemoveMemberCallback = new Callback<Void>() {
        @Override
        public void onResponse(Response<Void> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }

            if (!response.isSuccess()) {
                Timber.e("Remove member response was not a success: %d", response.code());
                Snackbar.make(mRoot, R.string.failed_to_remove_member, Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }

            mAdapter.removeMember(mMember);
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);

            if (getView() == null) {
                return;
            }

            Snackbar.make(mRoot, R.string.connection_error, Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private final AccessDialog.OnAccessChangedListener mOnAccessChangedListener = new AccessDialog.OnAccessChangedListener() {
        @Override
        public void onAccessChanged(Member member, String accessLevel) {
            loadData();
        }
    };

    private final MemberAdapter.Listener mMemberAdapterListener = new MemberAdapter.Listener() {
        @Override
        public void onProjectMemberClicked(Member member, ProjectMemberViewHolder memberGroupViewHolder) {
            NavigationManager.navigateToUser(getActivity(), memberGroupViewHolder.mImageView, member);
        }

        @Override
        public void onRemoveMember(Member member) {
            mMember = member;
            GitLabClient.instance().removeProjectMember(mProject.getId(), member.getId()).enqueue(mRemoveMemberCallback);
        }

        @Override
        public void onChangeAccess(Member member) {
            AccessDialog accessDialog = new AccessDialog(getActivity(), member, mProject.getId());
            accessDialog.setOnAccessChangedListener(mOnAccessChangedListener);
            accessDialog.show();
        }

        @Override
        public void onSeeGroupClicked() {
            NavigationManager.navigateToGroup(getActivity(), mProject.getNamespace().getId());
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_members, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mEventReceiver = new EventReceiver();
        GitLabApp.bus().register(mEventReceiver);

        mAdapter = new MemberAdapter(mMemberAdapterListener);
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
        ButterKnife.unbind(this);
        GitLabApp.bus().unregister(mEventReceiver);
    }

    @OnClick(R.id.add_user_button)
    public void onAddUserClick() {
        NavigationManager.navigateToAddProjectMember(getActivity(), mProject.getId());
    }

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

        GitLabClient.instance().getProjectMembers(mProject.getId()).enqueue(mProjectMembersCallback);
    }

    private void loadMore() {
        if (mNextPageUrl == null) {
            return;
        }

        Timber.d("loadMore called for " + mNextPageUrl);
        GitLabClient.instance().getProjectMembers(mNextPageUrl.toString()).enqueue(mMoreProjectMembersCallback);
    }

    private void setNamespace() {
        if (mProject == null) {
            return;
        }

        //If there is an owner, then there is no group
        if (mProject.getOwner() != null) {
            mAdapter.setNamespace(null);
        } else {
            mAdapter.setNamespace(mProject.getNamespace());
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
                mMessageView.setVisibility(View.GONE);
            }
        }
    }
}