package com.commit451.gitlab.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.CommitsAdapter;
import com.commit451.gitlab.model.DiffLine;
import com.commit451.gitlab.tools.Repository;
import com.commit451.gitlab.tools.RetrofitHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CommitsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

	@Bind(R.id.list) RecyclerView listView;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;
	
	public CommitsFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_commits, container, false);
		ButterKnife.bind(this, view);

		listView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
		if(Repository.selectedProject == null)
			return;

		if(Repository.selectedBranch == null) {
            if(swipeLayout != null && swipeLayout.isRefreshing())
                swipeLayout.setRefreshing(false);

            listView.setAdapter(null);
            return;
        }
		
		if(swipeLayout != null && !swipeLayout.isRefreshing())
            swipeLayout.setRefreshing(true);
		
		Repository.getService().getCommits(Repository.selectedProject.getId(), Repository.selectedBranch.getName(), commitsCallback);
	}

	public boolean onBackPressed() {
		return false;
	}
	
	private Callback<List<DiffLine>> commitsCallback = new Callback<List<DiffLine>>() {
		
		@Override
		public void success(List<DiffLine> commits, Response resp) {
            if (swipeLayout == null) {
                return;
            }
            swipeLayout.setRefreshing(false);
			
			if(commits.size() > 0) {
				Repository.newestCommit = commits.get(0);
			}
			else {
				Repository.newestCommit = null;
			}
			listView.setAdapter(new CommitsAdapter(commits));
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(getActivity(), e);

			if(swipeLayout != null && swipeLayout.isRefreshing())
                swipeLayout.setRefreshing(false);

			Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_commits), Snackbar.LENGTH_SHORT)
					.show();
			listView.setAdapter(null);
		}
	};
}