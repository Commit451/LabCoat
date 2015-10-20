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
import com.commit451.gitlab.events.IssueChangedEvent;
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

    @Bind(R.id.titleInputLayout) TextInputLayout mTitleInputLayout;
    @Bind(R.id.title_input) EditText mTitleInput;
    @Bind(R.id.descriptionInputLayout) TextInputLayout mDescriptionInputLayout;
    @Bind(R.id.description_input) EditText mDescriptionInput;
    @Bind(R.id.progress) View mProgress;

    private Project mProject;
    private Issue mIssue;

    public NewIssueDialog(Context context, Project project) {
        super(context);
        setContentView(R.layout.dialog_add_issue);
        ButterKnife.bind(this);
        mProject = project;
    }

    public NewIssueDialog(Context context, Project project, Issue issue) {
        super(context);
        setContentView(R.layout.dialog_add_issue);
        ButterKnife.bind(this);
        mProject = project;
        mIssue = issue;
        bindIssue();
    }

    @OnClick(R.id.save_button)
    public void onSaveClick() {
        if(!TextUtils.isEmpty(mTitleInput.getText())) {
            mTitleInputLayout.setError(null);
            mProgress.setVisibility(View.VISIBLE);
            mProgress.setAlpha(0.0f);
            mProgress.animate().alpha(1.0f);
            if (mIssue == null) {
                GitLabClient.instance().postIssue(mProject.getId(), mTitleInput.getText().toString().trim(), mDescriptionInput.getText().toString().trim())
                        .enqueue(mIssueCallback);
            } else {
                GitLabClient.instance().updateIssue(mProject.getId(), mIssue.getId(), mTitleInput.getText().toString(), mDescriptionInput.getText().toString())
                        .enqueue(mIssueCallback);
            }
        }
        else {
            mTitleInputLayout.setError(getContext().getString(R.string.required_field));
        }
    }

    @OnClick(R.id.cancel_button)
    public void onCancelClick() {
        this.dismiss();
    }

    private Callback<Issue> mIssueCallback = new Callback<Issue>() {

        @Override
        public void onResponse(Response<Issue> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                return;
            }
            if (mIssue == null) {
                GitLabApp.bus().post(new IssueCreatedEvent(response.body()));
            } else {
                GitLabApp.bus().post(new IssueChangedEvent(response.body()));
            }
            dismiss();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t.toString());
            mProgress.setVisibility(View.GONE);
            Toast.makeText(getContext(), getContext().getString(R.string.connection_error), Toast.LENGTH_SHORT)
                    .show();
        }
    };

    private void bindIssue() {
        if (!TextUtils.isEmpty(mIssue.getTitle())) {
            mTitleInput.setText(mIssue.getTitle());
        }
        if (!TextUtils.isEmpty(mIssue.getDescription())) {
            mDescriptionInput.setText(mIssue.getDescription());
        }
    }
}
