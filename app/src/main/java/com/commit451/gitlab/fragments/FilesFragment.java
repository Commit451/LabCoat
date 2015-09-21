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
import com.commit451.gitlab.activities.FileActivity;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.events.ProjectChangedEvent;
import com.commit451.gitlab.model.TreeItem;
import com.commit451.gitlab.viewHolders.FileViewHolder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

public class FilesFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

	public static FilesFragment newInstance() {
		
		Bundle args = new Bundle();
		
		FilesFragment fragment = new FilesFragment();
		fragment.setArguments(args);
		return fragment;
	}
	
	private ArrayList<String> path;

	@Bind(R.id.error_text) TextView errorText;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;
	@Bind(R.id.list) RecyclerView list;

	EventReceiver eventReceiver;
	
	public FilesFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_files, container, false);
		ButterKnife.bind(this, view);

		list.setLayoutManager(new LinearLayoutManager(getActivity()));

        swipeLayout.setOnRefreshListener(this);
		
		if(GitLabApp.instance().getSelectedProject() != null) {
			loadData();
		}

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
	protected void loadData() {
        Timber.d("loadData");
        path = new ArrayList<>();
		loadFiles();
	}
	
	@Override
	public void onRefresh() {
		loadFiles();
	}
	
	private void loadFiles() {
		String branch = "master";
		if(GitLabApp.instance().getSelectedBranch() != null) {
			branch = GitLabApp.instance().getSelectedBranch().getName();
		}
		
		if(swipeLayout != null && !swipeLayout.isRefreshing()) {
			swipeLayout.setRefreshing(true);
		}
		
		String currentPath = "";
        for(String p : path) {
            currentPath += p;
        }

		GitLabClient.instance().getTree(GitLabApp.instance().getSelectedProject().getId(), branch, currentPath).enqueue(filesCallback);
	}
	
	private Callback<List<TreeItem>> filesCallback = new Callback<List<TreeItem>>() {


		@Override
		public void onResponse(Response<List<TreeItem>> response) {
			if (!response.isSuccess()) {
				if(response.code() == 404) {
					errorText.setVisibility(View.VISIBLE);
					list.setVisibility(View.GONE);
				}
				else {
					if(path.size() > 0) {
						path.remove(path.size() - 1);
					}
					list.setAdapter(null);

					if(response.code() != 500) {
						Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_files), Snackbar.LENGTH_SHORT)
								.show();
					}
				}
				return;
			}
			if(swipeLayout != null && swipeLayout.isRefreshing()) {
				swipeLayout.setRefreshing(false);
			}
			if (response.body() != null && !response.body().isEmpty()) {
				list.setVisibility(View.VISIBLE);
				list.setAdapter(new FilesAdapter(response.body()));
				errorText.setVisibility(View.GONE);
			} else {
				errorText.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onFailure(Throwable t) {
			if(swipeLayout != null && swipeLayout.isRefreshing()) {
				swipeLayout.setRefreshing(false);
			}
			Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_files), Snackbar.LENGTH_SHORT)
					.show();
			Timber.e(t.toString());

		}
	};
	
	public boolean onBackPressed() {
		if(path.size() > 0) {
			path.remove(path.size() - 1);
			loadFiles();
			return true;
		}
		
		return false;
	}

	private class EventReceiver {

		@Subscribe
		public void onProjectChanged(ProjectChangedEvent event) {
			loadData();
		}
	}

	public class FilesAdapter extends RecyclerView.Adapter<FileViewHolder> {

		private List<TreeItem> mValues;

		public TreeItem getValueAt(int position) {
			return mValues.get(position);
		}

		public FilesAdapter(List<TreeItem> items) {
			mValues = items;
		}

		private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = (int) v.getTag(R.id.list_position);
				TreeItem treeItem = getValueAt(position);

				if(treeItem.getType().equals("tree")) {
					path.add(treeItem.getName() + "/");
					loadFiles();
				}
				else if(treeItem.getType().equals("blob")) {
					String pathExtra = "";
					for(String p : path) {
						pathExtra += p;
					}
                    pathExtra = pathExtra + treeItem.getName();
					startActivity(FileActivity.newIntent(getActivity(), GitLabApp.instance().getSelectedProject().getId(),
                            pathExtra, GitLabApp.instance().getSelectedBranch().getName()));
				}
			}
		};

		@Override
		public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			FileViewHolder holder = FileViewHolder.create(parent);
			holder.itemView.setOnClickListener(onProjectClickListener);
			return holder;
		}

		@Override
		public void onBindViewHolder(final FileViewHolder holder, int position) {
			TreeItem treeItem = getValueAt(position);
			holder.bind(treeItem);
			holder.itemView.setTag(R.id.list_position, position);
		}

		@Override
		public int getItemCount() {
			return mValues.size();
		}
	}
}