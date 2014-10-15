package com.bd.gitlab.fragments;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.melnykov.fab.FloatingActionButton;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.bd.gitlab.IssueActivity;
import com.bd.gitlab.R;
import com.bd.gitlab.adapter.IssuesAdapter;
import com.bd.gitlab.model.Issue;
import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.tools.RetrofitHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class IssuesFragment extends Fragment implements OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

	@InjectView(R.id.add_issue_button) FloatingActionButton addIssueButton;
	@InjectView(R.id.fragmentList) ListView listView;
    @InjectView(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;
	
	public IssuesFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_issues, container, false);
		ButterKnife.inject(this, view);
		
		listView.setOnItemClickListener(this);
        addIssueButton.attachToListView(listView);

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
		
		if(Repository.selectedProject != null)
			loadData();
		
		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
	public void onRefresh() {
		loadData();
	}
	
	public void loadData() {
		if(swipeLayout != null && !swipeLayout.isRefreshing())
            swipeLayout.setRefreshing(true);
		
		Repository.getService().getIssues(Repository.selectedProject.getId(), issuesCallback);
	}
	
	private Callback<List<Issue>> issuesCallback = new Callback<List<Issue>>() {
		
		@Override
		public void success(List<Issue> issues, Response resp) {
			if(swipeLayout != null && swipeLayout.isRefreshing())
                swipeLayout.setRefreshing(false);
			
			IssuesAdapter issueAdapter = new IssuesAdapter(getActivity(), issues);
			listView.setAdapter(issueAdapter);
			
			Repository.issueAdapter = issueAdapter;

			addIssueButton.setEnabled(true);
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(getActivity(), e);
			
			if(swipeLayout != null && swipeLayout.isRefreshing())
                swipeLayout.setRefreshing(false);
			
			Crouton.makeText(getActivity(), R.string.connection_error_issues, Style.ALERT).show();
			listView.setAdapter(null);
		}
	};
	
	public boolean onBackPressed() {
		return false;
	}

	@OnClick(R.id.add_issue_button)
	public void onAddIssueClick() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		DialogFragment newFragment = AddIssueDialogFragment.newInstance();
		newFragment.show(ft, "dialog");
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Repository.selectedIssue = ((IssuesAdapter) listView.getAdapter()).getItem(position);
		startActivity(new Intent(getActivity(), IssueActivity.class));
	}
}