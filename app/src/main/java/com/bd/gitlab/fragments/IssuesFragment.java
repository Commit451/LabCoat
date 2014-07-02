package com.bd.gitlab.fragments;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.bd.gitlab.IssueActivity;
import com.bd.gitlab.R;
import com.bd.gitlab.adapter.IssuesAdapter;
import com.bd.gitlab.model.Issue;
import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.tools.RetrofitHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class IssuesFragment extends Fragment implements OnItemClickListener, OnRefreshListener {
	
	@InjectView(R.id.add_issue_button) Button addIssueButton;
	@InjectView(R.id.fragmentList) ListView listView;
    @InjectView(R.id.ptr_layout) PullToRefreshLayout ptrLayout;
	
	public IssuesFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_issues, container, false);
		ButterKnife.inject(this, view);
		
		listView.setOnItemClickListener(this);

        ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable().listener(this).setup(ptrLayout);
		
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
	public void onRefreshStarted(View view) {
		loadData();
	}
	
	public void loadData() {
		if(ptrLayout != null && !ptrLayout.isRefreshing())
            ptrLayout.setRefreshing(true);
		
		Repository.getService().getIssues(Repository.selectedProject.getId(), issuesCallback);
	}
	
	private Callback<List<Issue>> issuesCallback = new Callback<List<Issue>>() {
		
		@Override
		public void success(List<Issue> issues, Response resp) {
			if(ptrLayout != null && ptrLayout.isRefreshing())
                ptrLayout.setRefreshComplete();
			
			IssuesAdapter issueAdapter = new IssuesAdapter(getActivity(), issues);
			listView.setAdapter(issueAdapter);
			
			Repository.issueAdapter = issueAdapter;
			
			addIssueButton.setEnabled(true);
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(getActivity(), e);
			
			if(ptrLayout != null && ptrLayout.isRefreshing())
                ptrLayout.setRefreshComplete();
			
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