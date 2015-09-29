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
import com.commit451.gitlab.activities.AddUserActivity;
import com.commit451.gitlab.activities.ProjectActivity;
import com.commit451.gitlab.adapter.MemberAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.events.ProjectReloadEvent;
import com.commit451.gitlab.events.UserAddedEvent;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.User;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

public class MembersFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {


    public static MembersFragment newInstance() {

        Bundle args = new Bundle();

        MembersFragment fragment = new MembersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.add_user_button) View addUserButton;
	@Bind(R.id.list) RecyclerView listView;
    MemberAdapter mAdapter;
	@Bind(R.id.error_text) TextView errorText;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;

    Project mProject;
	EventReceiver eventReceiver;

	public MembersFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_members, container, false);
        ButterKnife.bind(this, view);

        mAdapter = new MemberAdapter(new MemberAdapter.Listener() {
            @Override
            public void onUserClicked(User user) {
                //TODO go to profile or allow kicking from group or something
            }
        });
		listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(mAdapter);
        swipeLayout.setOnRefreshListener(this);

		eventReceiver = new EventReceiver();
		GitLabApp.bus().register(eventReceiver);

		if (getActivity() instanceof ProjectActivity) {
			mProject = ((ProjectActivity) getActivity()).getProject();
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
		GitLabApp.bus().unregister(eventReceiver);
	}
	
	@Override
	public void onRefresh() {
		loadData();
	}
	
	public void loadData() {
        swipeLayout.setRefreshing(true);
        GitLabClient.instance().getGroupMembers(mProject.getNamespace().getId()).enqueue(usersCallback);
	}
	
	public Callback<List<User>> usersCallback = new Callback<List<User>>() {

		@Override
		public void onResponse(Response<List<User>> response) {
			if (!response.isSuccess()) {
				return;
			}
			if (getView() == null) {
				return;
			}
			swipeLayout.setRefreshing(false);
			errorText.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			addUserButton.setVisibility(View.VISIBLE);

            mAdapter.setData(response.body());

			addUserButton.setEnabled(true);
		}

		@Override
		public void onFailure(Throwable t) {
			if (getView() == null) {
				return;
			}
			swipeLayout.setRefreshing(false);
			errorText.setVisibility(View.VISIBLE);
			addUserButton.setVisibility(View.GONE);
			Timber.e(t.toString());
			Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_users), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
	
	public boolean onBackPressed() {
		return false;
	}
	
	@OnClick(R.id.add_user_button)
	public void onAddUserClick() {
		startActivity(AddUserActivity.newInstance(getActivity()));
	}

	private class EventReceiver {

		@Subscribe
		public void onProjectChanged(ProjectReloadEvent event) {
            mProject = event.project;
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