package com.commit451.gitlab.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.commit451.gitlab.IssueActivity;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.tools.Repository;
import com.commit451.gitlab.tools.RetrofitHelper;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddIssueDialogFragment extends DialogFragment {
	
	@Bind(R.id.title_input)
	EditText titleInput;
	@Bind(R.id.description_input)
	EditText descriptionInput;
	
	private ProgressDialog pd;
	
	/**
	 * Create a new instance of AddDialogFragment
	 **/
	static AddIssueDialogFragment newInstance() {
		return new AddIssueDialogFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_add_issue, container, false);
		ButterKnife.bind(this, view);
		
		getDialog().setTitle(getString(R.string.add_issue_dialog_title));
		
		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
        ButterKnife.unbind(this);
	}
	
	@OnClick(R.id.save_button)
	public void onSaveClick() {
		if(titleInput.getText().toString().trim().length() > 0) {
			pd = ProgressDialog.show(AddIssueDialogFragment.this.getActivity(), "", getResources().getString(R.string.progress_dialog), true);
			GitLabClient.instance().postIssue(Repository.selectedProject.getId(), titleInput.getText().toString().trim(), descriptionInput.getText().toString().trim(), "", issueCallback);
		}
		else {
			Toast.makeText(getActivity(), getString(R.string.connection_error), Toast.LENGTH_SHORT)
					.show();
		}
	}
	
	@OnClick(R.id.cancel_button)
	public void onCancelClick() {
		this.dismiss();
	}
	
	private Callback<Issue> issueCallback = new Callback<Issue>() {
		
		@Override
		public void success(Issue issue, Response resp) {
			if(pd != null && pd.isShowing()) {
				pd.cancel();
			}

			//TODO update the parent list when a new issue is created
			Repository.selectedIssue = issue;
			startActivity(new Intent(getActivity(), IssueActivity.class));
			
			AddIssueDialogFragment.this.dismiss();
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(getActivity(), e);
			
			if(pd != null && pd.isShowing()) {
				pd.cancel();
			}
			Toast.makeText(getActivity(), getString(R.string.connection_error), Toast.LENGTH_SHORT)
					.show();
		}
	};
}
