package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.IssueChangedEvent;
import com.commit451.gitlab.event.IssueCreatedEvent;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Project;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Dialog to input new issues, but not really a dialog at all wink wink
 */
public class AddIssueActivity extends MorphActivity {

    private static final String KEY_PROJECT = "project";
    private static final String KEY_ISSUE = "issue";

    public static Intent newIntent(Context context, Project project, Issue issue) {
        Intent intent = new Intent(context, AddIssueActivity.class);
        intent.putExtra(KEY_PROJECT, Parcels.wrap(project));
        if (issue != null) {
            intent.putExtra(KEY_ISSUE, Parcels.wrap(issue));
        }
        return intent;
    }

    @Bind(R.id.root) ViewGroup mRoot;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.title_text_input_layout) TextInputLayout mTitleInputLayout;
    @Bind(R.id.title) EditText mTitleInput;
    @Bind(R.id.description) EditText mDescriptionInput;
    @Bind(R.id.progress) View mProgress;
    @Bind(R.id.assignee_progress) View mAssigneeProgress;
    @Bind(R.id.assignee_spinner) Spinner mAssigneeSpinner;
    @Bind(R.id.milestone_progress) View mMilestoneProgress;
    @Bind(R.id.milestone_spinner) Spinner mMilestoneSpinner;
    ArrayAdapter<Milestone> mMilestoneArrayAdapter;
    ArrayAdapter<Member> mAssigneeArrayAdapter;


    private Project mProject;
    private Issue mIssue;

    private final Callback<List<Milestone>> mMilestonesCallback = new Callback<List<Milestone>>() {
        @Override
        public void onResponse(Response<List<Milestone>> response, Retrofit retrofit) {
            mMilestoneProgress.setVisibility(View.GONE);
            if (!response.isSuccess()) {
                mMilestoneSpinner.setVisibility(View.GONE);
                return;
            }
            mMilestoneSpinner.setVisibility(View.VISIBLE);
            mMilestoneArrayAdapter.clear();
            mMilestoneArrayAdapter.addAll(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mMilestoneProgress.setVisibility(View.GONE);
            mMilestoneSpinner.setVisibility(View.GONE);
        }
    };

    private final Callback<List<Member>> mAssigneeCallback = new Callback<List<Member>>() {
        @Override
        public void onResponse(Response<List<Member>> response, Retrofit retrofit) {
            mAssigneeProgress.setVisibility(View.GONE);
            if (!response.isSuccess()) {
                mAssigneeSpinner.setVisibility(View.GONE);
                return;
            }
            mAssigneeSpinner.setVisibility(View.VISIBLE);
            mAssigneeArrayAdapter.clear();
            mAssigneeArrayAdapter.addAll(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mMilestoneProgress.setVisibility(View.GONE);
            mMilestoneSpinner.setVisibility(View.GONE);
        }
    };

    private final Callback<Issue> mIssueCreatedCallback = new Callback<Issue>() {

        @Override
        public void onResponse(Response<Issue> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                Toast.makeText(AddIssueActivity.this, getString(R.string.failed_to_create_issue), Toast.LENGTH_SHORT)
                        .show();
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
            Timber.e(t, null);
            Toast.makeText(AddIssueActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_issue);
        ButterKnife.bind(this);
        morph(mRoot);

        mProject = Parcels.unwrap(getIntent().getParcelableExtra(KEY_PROJECT));
        mIssue = Parcels.unwrap(getIntent().getParcelableExtra(KEY_ISSUE));

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_create:
                    case R.id.action_edit:
                        save();
                        return true;
                }
                return false;
            }
        });

        if (mIssue != null) {
            bindIssue();
            mToolbar.inflateMenu(R.menu.menu_edit_milestone);
        } else {
            mToolbar.inflateMenu(R.menu.menu_add_milestone);
        }

        mMilestoneArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        mMilestoneSpinner.setAdapter(mMilestoneArrayAdapter);
        mAssigneeArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        mAssigneeSpinner.setAdapter(mAssigneeArrayAdapter);
        load();
    }

    private void load() {
        GitLabClient.instance().getMilestones(mProject.getId()).enqueue(mMilestonesCallback);
        GitLabClient.instance().getProjectMembers(mProject.getId()).enqueue(mAssigneeCallback);
    }

    private void showLoading() {
        mProgress.setVisibility(View.VISIBLE);
        mProgress.setAlpha(0.0f);
        mProgress.animate().alpha(1.0f);
    }

    private void bindIssue() {
        if (!TextUtils.isEmpty(mIssue.getTitle())) {
            mTitleInput.setText(mIssue.getTitle());
        }
        if (!TextUtils.isEmpty(mIssue.getDescription())) {
            mDescriptionInput.setText(mIssue.getDescription());
        }
    }

    private void save() {
        if(!TextUtils.isEmpty(mTitleInput.getText())) {
            mTitleInputLayout.setError(null);
            showLoading();
            if (mIssue == null) {
                GitLabClient.instance().createIssue(mProject.getId(), mTitleInput.getText().toString().trim(), mDescriptionInput.getText().toString().trim())
                        .enqueue(mIssueCreatedCallback);
            } else {
                GitLabClient.instance().updateIssue(mProject.getId(), mIssue.getId(), mTitleInput.getText().toString(), mDescriptionInput.getText().toString())
                        .enqueue(mIssueCreatedCallback);
            }
        }
        else {
            mTitleInputLayout.setError(getString(R.string.required_field));
        }
    }

}