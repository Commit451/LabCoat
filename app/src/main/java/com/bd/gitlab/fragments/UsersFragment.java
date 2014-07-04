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
import android.widget.TextView;

import com.bd.gitlab.R;
import com.bd.gitlab.adapter.UserAdapter;
import com.bd.gitlab.model.User;
import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.tools.RetrofitHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class UsersFragment extends Fragment implements OnItemClickListener, OnRefreshListener {
	
	@InjectView(R.id.add_user_button) Button addUserButton;
	@InjectView(R.id.fragmentList) ListView listView;
	@InjectView(R.id.error_text) TextView errorText;
    @InjectView(R.id.ptr_layout) PullToRefreshLayout ptrLayout;
	
	public UsersFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_users, container, false);
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
		
		if(Repository.selectedProject.getGroup() == null) {
			errorText.setVisibility(View.VISIBLE);
			errorText.setText(R.string.not_in_group);
			listView.setVisibility(View.GONE);
			addUserButton.setVisibility(View.GONE);
			if(ptrLayout != null && ptrLayout.isRefreshing())
				ptrLayout.setRefreshComplete();
			return;
		}
		
		Repository.getService().getGroupMembers(Repository.selectedProject.getGroup().getId(), usersCallback);
	}
	
	public Callback<List<User>> usersCallback = new Callback<List<User>>() {
		
		@Override
		public void success(List<User> users, Response resp) {
			if(ptrLayout != null && ptrLayout.isRefreshing())
                ptrLayout.setRefreshComplete();
			
			errorText.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			addUserButton.setVisibility(View.VISIBLE);
			
			Repository.userAdapter = new UserAdapter(getActivity(), users);
			listView.setAdapter(Repository.userAdapter);
			
			addUserButton.setEnabled(true);
		}
		
		@Override
		public void failure(RetrofitError e) {
			if(ptrLayout != null && ptrLayout.isRefreshing())
                ptrLayout.setRefreshComplete();
			
			if(e.getResponse() != null && e.getResponse().getStatus() == 404) {
				errorText.setVisibility(View.VISIBLE);
				errorText.setText(R.string.groups_not_supported);
				listView.setVisibility(View.GONE);
				addUserButton.setVisibility(View.GONE);
			}
			else {
				errorText.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				addUserButton.setVisibility(View.VISIBLE);

                RetrofitHelper.printDebugInfo(getActivity(), e);
				Crouton.makeText(getActivity(), R.string.connection_error_users, Style.ALERT).show();
				listView.setAdapter(null);
			}
		}
	};
	
	public boolean onBackPressed() {
		return false;
	}
	
	@OnClick(R.id.add_user_button)
	public void onAddUserClick() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		DialogFragment newFragment = AddUserDialogFragment.newInstance();
		newFragment.show(ft, "dialog");
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Repository.selectedUser = Repository.userAdapter.getItem(position);
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		DialogFragment newFragment = RemoveUserDialogFragment.newInstance();
		newFragment.show(ft, "dialog");
	}
}