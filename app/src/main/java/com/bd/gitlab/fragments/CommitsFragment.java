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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.bd.gitlab.DiffActivity;
import com.bd.gitlab.R;
import com.bd.gitlab.adapter.CommitsAdapter;
import com.bd.gitlab.model.DiffLine;
import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.tools.RetrofitHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CommitsFragment extends Fragment implements OnRefreshListener, OnItemClickListener {
	
	@InjectView(R.id.fragmentList) ListView listView;
	@InjectView(R.id.repo_url) EditText repoUrl;
    @InjectView(R.id.ptr_layout) PullToRefreshLayout ptrLayout;
	
	public CommitsFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_commits, container, false);
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
		if(Repository.selectedProject == null)
			return;
		
		repoUrl.setText("git@" + Repository.getServerUrl().replaceAll("http://", "").replaceAll("https://", "") + ":" + Repository.selectedProject.getPathWithNamespace() + ".git");
		
		if(Repository.selectedBranch == null) {
            if(ptrLayout != null && ptrLayout.isRefreshing())
                ptrLayout.setRefreshComplete();

            listView.setAdapter(null);
            return;
        }
		
		if(ptrLayout != null && !ptrLayout.isRefreshing())
            ptrLayout.setRefreshing(true);
		
		Repository.getService().getCommits(Repository.selectedProject.getId(), Repository.selectedBranch.getName(), commitsCallback);
	}
	
	private Callback<List<DiffLine>> commitsCallback = new Callback<List<DiffLine>>() {
		
		@Override
		public void success(List<DiffLine> commits, Response resp) {
			if(ptrLayout != null && ptrLayout.isRefreshing())
                ptrLayout.setRefreshComplete();
			
			if(commits.size() > 0)
				Repository.newestCommit = commits.get(0);
			else
				Repository.newestCommit = null;
			
			CommitsAdapter commitsAdapter = new CommitsAdapter(getActivity(), commits);
			listView.setAdapter(commitsAdapter);
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(getActivity(), e);

			if(ptrLayout != null && ptrLayout.isRefreshing())
                ptrLayout.setRefreshComplete();

			Crouton.makeText(getActivity(), R.string.connection_error_commits, Style.ALERT).show();
			listView.setAdapter(null);
		}
	};

	@OnClick(R.id.repo_url)
	public void onRepoClick() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", repoUrl.getText().toString());
        clipboard.setPrimaryClip(clip);
		
		Crouton.makeText(this.getActivity(), R.string.copy_notification, Style.CONFIRM).show();
	}
	
	public boolean onBackPressed() {
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Repository.selectedCommit = ((CommitsAdapter) listView.getAdapter()).getItem(position);
		startActivity(new Intent(getActivity(), DiffActivity.class));
	}
}