package com.commit451.gitlab.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.UserAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.Repository;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

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

		GitLabClient.instance().addGroupMember(Repository.selectedProject.getGroup().getId(), userId, accessLevel, "", userCallback);
	}
	
	private Callback<User> userCallback = new Callback<User>() {
		
		@Override
		public void success(User user, Response resp) {
			if(pd != null && pd.isShowing())
				pd.cancel();
			
			if(user.getId() != 0) {
				//TODO tell the parent to add the user to the list
			}
			else {
				Toast.makeText(getActivity(), getString(R.string.user_error), Toast.LENGTH_SHORT)
						.show();
			}
			
			AddUserDialogFragment.this.dismiss();
		}
		
		@Override
		public void failure(RetrofitError e) {
			Timber.e(e.toString());
			
			if(pd != null && pd.isShowing())
				pd.cancel();
			Toast.makeText(getActivity(), getString(R.string.user_error), Toast.LENGTH_SHORT)
					.show();
			AddUserDialogFragment.this.dismiss();
		}
	};
	
	@OnClick(R.id.cancel_button)
	public void onCancelClick() {
		this.dismiss();
	}
}
