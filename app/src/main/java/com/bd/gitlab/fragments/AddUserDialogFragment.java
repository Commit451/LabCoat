package com.bd.gitlab.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.bd.gitlab.R;
import com.bd.gitlab.adapter.UserAdapter;
import com.bd.gitlab.model.User;
import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.tools.RetrofitHelper;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddUserDialogFragment extends DialogFragment {
	
	@Bind(R.id.user_spinner)
	Spinner userSpinner;
	@Bind(R.id.role_spinner)
	Spinner roleSpinner;
	
	private ProgressDialog pd;
	
	/**
	 * Create a new instance of AddDialogFragment
	 **/
	static AddUserDialogFragment newInstance() {
		return new AddUserDialogFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_add_user, container, false);
		ButterKnife.bind(this, view);
		
		getDialog().setTitle(getString(R.string.add_user_dialog_title));
		
		UserAdapter adapter = new UserAdapter(this.getActivity(), Repository.users);
		userSpinner.setAdapter(adapter);
		
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this.getActivity(), R.array.role_names, android.R.layout.simple_spinner_dropdown_item);
		roleSpinner.setAdapter(adapter2);
		
		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
        ButterKnife.unbind(this);
	}
	
	@OnClick(R.id.add_button)
	public void onAddClick() {
		if(Repository.selectedProject.getGroup() == null)
			return;
		
		pd = ProgressDialog.show(AddUserDialogFragment.this.getActivity(), "", getResources().getString(R.string.progress_dialog), true);
		
		long userId = ((User) userSpinner.getSelectedItem()).getId();
		String accessLevel = getActivity().getResources().getStringArray(R.array.role_values)[roleSpinner.getSelectedItemPosition()];
		
		Repository.getService().addGroupMember(Repository.selectedProject.getGroup().getId(), userId, accessLevel, userCallback);
	}
	
	private Callback<User> userCallback = new Callback<User>() {
		
		@Override
		public void success(User user, Response resp) {
			if(pd != null && pd.isShowing())
				pd.cancel();
			
			if(user.getId() != 0)
				Repository.userAdapter.addUser(user);
			else
				Crouton.makeText(AddUserDialogFragment.this.getActivity(), R.string.user_error, Style.ALERT).show();
			
			AddUserDialogFragment.this.dismiss();
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(getActivity(), e);
			
			if(pd != null && pd.isShowing())
				pd.cancel();
			
			Crouton.makeText(AddUserDialogFragment.this.getActivity(), R.string.user_error, Style.ALERT).show();
			AddUserDialogFragment.this.dismiss();
		}
	};
	
	@OnClick(R.id.cancel_button)
	public void onCancelClick() {
		this.dismiss();
	}
}
