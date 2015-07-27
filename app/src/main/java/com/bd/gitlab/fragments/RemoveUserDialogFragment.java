package com.bd.gitlab.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bd.gitlab.R;
import com.bd.gitlab.model.DeleteResponse;
import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.tools.RetrofitHelper;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RemoveUserDialogFragment extends DialogFragment {
	
	private ProgressDialog pd;
	
	/**
	 * Create a new instance of AddDialogFragment
	 **/
	static RemoveUserDialogFragment newInstance() {
		return new RemoveUserDialogFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_remove_user, container, false);
		ButterKnife.bind(this, view);
		
		getDialog().setTitle(getString(R.string.remove_user_dialog_title));
		
		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
        ButterKnife.unbind(this);
	}
	
	@OnClick(R.id.remove_button)
	public void onRemoveClick() {
		if(Repository.selectedProject.getGroup() == null)
			return;
		
		pd = ProgressDialog.show(RemoveUserDialogFragment.this.getActivity(), "", getResources().getString(R.string.progress_dialog), true);
		
		Repository.getService().removeGroupMember(Repository.selectedProject.getGroup().getId(), Repository.selectedUser.getId(), deleteCallback);
	}
	
	private Callback<DeleteResponse> deleteCallback = new Callback<DeleteResponse>() {
		
		@Override
		public void success(DeleteResponse response, Response resp) {
			if(pd != null && pd.isShowing())
				pd.cancel();
			
			if(response.getUserId() != 0) {
				Repository.userAdapter.removeUser(response.getUserId());
			}
			else
				Crouton.makeText(RemoveUserDialogFragment.this.getActivity(), R.string.user_remove_error, Style.ALERT).show();
			
			RemoveUserDialogFragment.this.dismiss();
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(getActivity(), e);
			
			if(pd != null && pd.isShowing())
				pd.cancel();
			
			Crouton.makeText(RemoveUserDialogFragment.this.getActivity(), R.string.user_remove_error, Style.ALERT).show();
			RemoveUserDialogFragment.this.dismiss();
		}
	};
	
	@OnClick(R.id.cancel_button)
	public void onCancelClick() {
		this.dismiss();
	}
}
