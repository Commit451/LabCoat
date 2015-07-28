package com.commit451.gitlab.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.FileActivity;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.TreeItem;
import com.commit451.gitlab.tools.Repository;
import com.commit451.gitlab.tools.RetrofitHelper;
import com.commit451.gitlab.viewHolders.FileViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class FilesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
	
	private ArrayList<String> path;

	@Bind(R.id.error_text) TextView errorText;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;
	@Bind(R.id.list) RecyclerView list;
	
	public FilesFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_files, container, false);
		ButterKnife.bind(this, view);

		list.setLayoutManager(new LinearLayoutManager(getActivity()));

        swipeLayout.setOnRefreshListener(this);

		path = new ArrayList<>();
		
		if(Repository.selectedProject != null) {
			loadData();
		}
		
		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
        ButterKnife.unbind(this);
	}
	
	public void loadData() {
		path = new ArrayList<>();
		loadFiles();
	}
	
	@Override
	public void onRefresh() {
		loadFiles();
	}
	
	private void loadFiles() {
		String branch = "master";
		if(Repository.selectedBranch != null)
			branch = Repository.selectedBranch.getName();
		
		if(swipeLayout != null && !swipeLayout.isRefreshing())
            swipeLayout.setRefreshing(true);
		
		String currentPath = "";
        for(String p : path) {
            currentPath += p;
        }
		
		Repository.getService().getTree(Repository.selectedProject.getId(), branch, currentPath, filesCallback);
	}
	
	private Callback<List<TreeItem>> filesCallback = new Callback<List<TreeItem>>() {
		
		@Override
		public void success(List<TreeItem> files, Response resp) {
            if(swipeLayout != null && swipeLayout.isRefreshing()) {
				swipeLayout.setRefreshing(false);
			}
			list.setAdapter(new FilesAdapter(files));
		}
		
		@Override
		public void failure(RetrofitError e) {
			if(swipeLayout != null && swipeLayout.isRefreshing())
                swipeLayout.setRefreshing(false);
			
			if(e.getResponse() != null && e.getResponse().getStatus() == 404) {
				errorText.setVisibility(View.VISIBLE);
				list.setVisibility(View.GONE);
			}
			else {
				if(path.size() > 0) {
					path.remove(path.size() - 1);
				}
				list.setAdapter(null);
				
				if(e.getResponse() != null && e.getResponse().getStatus() != 500) {
                    RetrofitHelper.printDebugInfo(getActivity(), e);
					Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_files), Snackbar.LENGTH_SHORT)
							.show();
                }
			}
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
				Repository.selectedFile = getValueAt(position);

				if(Repository.selectedFile.getType().equals("tree")) {
					path.add(Repository.selectedFile.getName() + "/");
					loadFiles();
				}
				else if(Repository.selectedFile.getType().equals("blob")) {
					String pathExtra = "";
					for(String p : path) {
						pathExtra += p;
					}

					Intent i = new Intent(getActivity(), FileActivity.class);
					i.putExtra("path", pathExtra);
					startActivity(i);
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