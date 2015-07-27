package com.commit451.gitlab.fragments;

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

import com.commit451.gitlab.IssueActivity;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.IssuesAdapter;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.tools.Repository;
import com.commit451.gitlab.tools.RetrofitHelper;
import com.melnykov.fab.FloatingActionButton;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class IssuesFragment extends Fragment implements OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

	@Bind(R.id.add_issue_button) FloatingActionButton addIssueButton;
	@Bind(R.id.fragmentList) ListView listView;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;
	
	public IssuesFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_issues, container, false);
		ButterKnife.bind(this, view);
		
		listView.setOnItemClickListener(this);
        addIssueButton.attachToListView(listView);

        swipeLayout.setOnRefreshListener(this);

		if(Repository.selectedProject != null)
			loadData();
		
		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
        ButterKnife.unbind(this);
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