package com.commit451.gitlab.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activities.IssueActivity;
import com.commit451.gitlab.adapter.IssuesAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.dialogs.NewIssueDialog;
import com.commit451.gitlab.events.IssueCreatedEvent;
import com.commit451.gitlab.events.ProjectReloadEvent;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.model.Project;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

public class IssuesFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

	public static IssuesFragment newInstance() {
		return new IssuesFragment();
	}

	@Bind(R.id.add_issue_button) View addIssueButton;
	@Bind(R.id.list) RecyclerView listView;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;

	IssuesAdapter issuesAdapter;
	EventReceiver eventReceiver;
    Project mProject;

	private final IssuesAdapter.Listener mIssuesAdapterListener = new IssuesAdapter.Listener() {
		@Override
		public void onIssueClicked(Issue issue) {
			getActivity().startActivity(IssueActivity.newInstance(getActivity(), mProject, issue));
		}
	};

	public IssuesFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_issues, container, false);
		ButterKnife.bind(this, view);

		listView.setLayoutManager(new LinearLayoutManager(getActivity()));
		issuesAdapter = new IssuesAdapter(mIssuesAdapterListener);
		listView.setAdapter(issuesAdapter);
        swipeLayout.setOnRefreshListener(this);

		eventReceiver = new EventReceiver();
		GitLabApp.bus().register(eventReceiver);
		
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
		if(swipeLayout != null && !swipeLayout.isRefreshing()) {
			swipeLayout.setRefreshing(true);
		}

		GitLabClient.instance().getIssues(mProject.getId()).enqueue(issuesCallback);
	}
	
	private Callback<List<Issue>> issuesCallback = new Callback<List<Issue>>() {

		@Override
		public void onResponse(Response<List<Issue>> response) {
			if (!response.isSuccess()) {
				return;
			}
			if(swipeLayout != null && swipeLayout.isRefreshing()) {
				swipeLayout.setRefreshing(false);
			}
			issuesAdapter.setIssues(response.body());

			addIssueButton.setEnabled(true);
		}

		@Override
		public void onFailure(Throwable t) {
			Timber.e(t.toString());

			if(swipeLayout != null && swipeLayout.isRefreshing())
				swipeLayout.setRefreshing(false);
			Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_issues), Snackbar.LENGTH_SHORT)
					.show();
			listView.setAdapter(null);
		}
	};
	
	public boolean onBackPressed() {
		return false;
	}

	@OnClick(R.id.add_issue_button)
	public void onAddIssueClick() {
		new NewIssueDialog(getActivity()).show();
	}

	private class EventReceiver {

        @Subscribe
        public void onReloadData(ProjectReloadEvent event) {
            mProject = event.project;
            loadData();
        }
		@Subscribe
		public void onIssueAdded(IssueCreatedEvent event) {
			issuesAdapter.addIssue(event.issue);
		}
	}
}