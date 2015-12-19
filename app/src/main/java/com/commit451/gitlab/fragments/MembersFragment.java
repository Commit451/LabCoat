package com.commit451.gitlab.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activities.ProjectActivity;
import com.commit451.gitlab.adapter.MemberAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.events.ProjectReloadEvent;
import com.commit451.gitlab.events.UserAddedEvent;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.NavigationManager;
import com.commit451.gitlab.viewHolders.MemberGroupViewHolder;
import com.commit451.gitlab.viewHolders.MemberProjectViewHolder;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class MembersFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {


    public static MembersFragment newInstance() {

        Bundle args = new Bundle();

        MembersFragment fragment = new MembersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.add_user_button) View mAddUserButton;
    @Bind(R.id.list) RecyclerView mRecyclerView;
    MemberAdapter mAdapter;
    @Bind(R.id.error_text) TextView mErrorText;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;

    Project mProject;
    EventReceiver mEventReceiver;

    private final Callback<List<User>> mProjectMemebersCallback = new Callback<List<User>>() {

        @Override
        public void onResponse(Response<List<User>> response, Retrofit retrofit) {
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

            mAdapter.setProjectMembers(response.body());

            mAddUserButton.setEnabled(true);
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
            Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_users), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private final Callback<List<User>> mGroupMembersCallback = new Callback<List<User>>() {

        @Override
        public void onResponse(Response<List<User>> response, Retrofit retrofit) {
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

            mAdapter.setGroupMembers(response.body());
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
            Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_users), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    public MembersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_members, container, false);
        ButterKnife.bind(this, view);

        mAdapter = new MemberAdapter(new MemberAdapter.Listener() {
            @Override
            public void onProjectMemberClicked(User user, MemberProjectViewHolder memberGroupViewHolder) {

            }

            @Override
            public void onGroupMemberClicked(User user, MemberGroupViewHolder memberGroupViewHolder) {

            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mEventReceiver = new EventReceiver();
        GitLabApp.bus().register(mEventReceiver);

        if (getActivity() instanceof ProjectActivity) {
            mProject = ((ProjectActivity) getActivity()).getProject();
            mAdapter.setProject(mProject);
            if (mProject != null) {
                loadData();
            }
        } else {
            throw new IllegalStateException("Incorrect parent activity");
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        GitLabApp.bus().unregister(mEventReceiver);
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    public void loadData() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        GitLabClient.instance().getProjectTeamMembers(mProject.getId()).enqueue(mProjectMemebersCallback);
        if (mProject.getNamespace() != null) {
            GitLabClient.instance().getGroupMembers(mProject.getNamespace().getId()).enqueue(mGroupMembersCallback);
        }
    }

    public boolean onBackPressed() {
        return false;
    }

    @OnClick(R.id.add_user_button)
    public void onAddUserClick() {
        NavigationManager.navigateToAddUser(getActivity(), mProject.getId());
    }

    private class EventReceiver {

        @Subscribe
        public void onProjectChanged(ProjectReloadEvent event) {
            mProject = event.project;
            mAdapter.setProject(mProject);
            loadData();
        }

        @Subscribe
        public void onUserAdded(UserAddedEvent event) {
            if (mAdapter != null) {
                mAdapter.addUser(event.user);
            }
        }
    }
}