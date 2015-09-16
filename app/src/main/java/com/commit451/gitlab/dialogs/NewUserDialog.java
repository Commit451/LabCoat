package com.commit451.gitlab.dialogs;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.UserAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.Repository;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

/**
 * Collabs FTW
 * Created by Jawn on 8/16/2015.
 */
public class NewUserDialog extends AppCompatDialog {

    @Bind(R.id.user_spinner) Spinner userSpinner;
    @Bind(R.id.role_spinner) Spinner roleSpinner;
    @Bind(R.id.progress) View progress;

    public NewUserDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_add_user);
        ButterKnife.bind(this);

        UserAdapter adapter = new UserAdapter(getContext(), Repository.users);
        userSpinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getContext(),
                R.array.role_names,
                android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter2);
    }

    @OnClick(R.id.add_button)
    public void onAddClick() {
        if(GitLabApp.instance().getSelectedProject().getGroup() == null) {
            return;
        }

        progress.setVisibility(View.VISIBLE);
        progress.setAlpha(0.0f);
        progress.animate().alpha(1.0f);

        long userId = ((User) userSpinner.getSelectedItem()).getId();
        String accessLevel = getContext().getResources().getStringArray(R.array.role_values)[roleSpinner.getSelectedItemPosition()];

        GitLabClient.instance().addGroupMember(GitLabApp.instance().getSelectedProject().getGroup().getId(), userId, accessLevel).enqueue(userCallback);
    }

    private Callback<User> userCallback = new Callback<User>() {

        @Override
        public void onResponse(Response<User> response) {
            if (!response.isSuccess()) {
                return;
            }
            progress.setVisibility(View.GONE);

            if(response.body().getId() != 0) {
                //TODO tell the parent to add the user to the list
            }
            else {
                Toast.makeText(getContext(), getContext().getString(R.string.user_error), Toast.LENGTH_SHORT)
                        .show();
            }
            dismiss();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t.toString());

            progress.setVisibility(View.GONE);
            Toast.makeText(getContext(), getContext().getString(R.string.user_error), Toast.LENGTH_SHORT)
                    .show();
            dismiss();
        }
    };

    @OnClick(R.id.cancel_button)
    public void onCancelClick() {
        this.dismiss();
    }
}
