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
import com.commit451.gitlab.adapter.CommitsAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.DiffLine;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

public class CommitsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

	@Bind(R.id.list) RecyclerView listView;
	CommitsAdapter adapter;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;
    @Bind(R.id.message_text) View messageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new CommitsAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_commits, container, false);
		ButterKnife.bind(this, view);

		listView.setLayoutManager(new LinearLayoutManager(getActivity()));
		listView.setAdapter(adapter);
        swipeLayout.setOnRefreshListener(this);

		if(GitLabApp.instance().getSelectedProject() != null) {
            loadData();
        }
		
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

    @Override
	protected void loadData() {
		if(GitLabApp.instance().getSelectedProject() == null) {
            return;
        }

		if(GitLabApp.instance().getSelectedBranch() == null) {
            if(swipeLayout != null && swipeLayout.isRefreshing()) {
                swipeLayout.setRefreshing(false);
            }

            adapter.setData(null);
            return;
        }
		
		if(swipeLayout != null && !swipeLayout.isRefreshing()) {
            swipeLayout.setRefreshing(true);
        }

        GitLabClient.instance().getCommits(GitLabApp.instance().getSelectedProject().getId(), GitLabApp.instance().getSelectedBranch().getName()).enqueue(commitsCallback);
	}

	public boolean onBackPressed() {
		return false;
	}
	
	private Callback<List<DiffLine>> commitsCallback = new Callback<List<DiffLine>>() {


		@Override
		public void onResponse(Response<List<DiffLine>> response) {
			if (!response.isSuccess()) {
				return;
			}
			if (swipeLayout == null) {
				return;
			}
			swipeLayout.setRefreshing(false);

			if(response.body().size() > 0) {
				messageView.setVisibility(View.GONE);
			}
			else {
				Timber.d("No commits have been made");
				messageView.setVisibility(View.VISIBLE);
			}
			adapter.setData(response.body());
		}

		@Override
		public void onFailure(Throwable t) {
			Timber.e(t.toString());

			if(swipeLayout != null && swipeLayout.isRefreshing()) {
				swipeLayout.setRefreshing(false);
			}
			messageView.setVisibility(View.VISIBLE);

			Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_commits), Snackbar.LENGTH_SHORT)
					.show();
			adapter.setData(null);
		}
	};
}