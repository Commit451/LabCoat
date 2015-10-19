package com.commit451.gitlab.dialogs;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.events.IssueCreatedEvent;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.model.Project;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * So many issues so little time
 * Created by Jawn on 8/16/2015.
 */
public class NewIssueDialog extends AppCompatDialog {

    @Bind(R.id.titleInputLayout) TextInputLayout titleInputLayout;
    @Bind(R.id.title_input) EditText titleInput;
    @Bind(R.id.descriptionInputLayout) TextInputLayout descriptionInputLayout;
    @Bind(R.id.description_input) EditText descriptionInput;
    @Bind(R.id.progress) View progress;

    private Project mProject;

    public NewIssueDialog(Context context, Project project) {
        super(context);
        setContentView(R.layout.dialog_add_issue);
        ButterKnife.bind(this);
        mProject = project;
    }

    @OnClick(R.id.save_button)
    public void onSaveClick() {
        if(!TextUtils.isEmpty(titleInput.getText())) {
            progress.setVisibility(View.VISIBLE);
            progress.setAlpha(0.0f);
            progress.animate().alpha(1.0f);
            GitLabClient.instance().postIssue(mProject.getId(), titleInput.getText().toString().trim(), descriptionInput.getText().toString().trim()).enqueue(issueCallback);
        }
        else {
            titleInputLayout.setError(getContext().getString(R.string.required_field));
        }
    }

    @OnClick(R.id.cancel_button)
    public void onCancelClick() {
        this.dismiss();
    }

    private Callback<Issue> issueCallback = new Callback<Issue>() {

        @Override
        public void onResponse(Response<Issue> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                return;
            }
            //TODO update the parent list when a new issue is created
            GitLabApp.bus().post(new IssueCreatedEvent(response.body()));
            //TODO fix this
//            getContext().startActivity(IssueActivity.newInstance(getContext(), response.body()));
            dismiss();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t.toString());
            progress.setVisibility(View.GONE);
            Toast.makeText(getContext(), getContext().getString(R.string.connection_error), Toast.LENGTH_SHORT)
                    .show();
        }
    };
}
