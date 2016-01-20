package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.AssigneeSpinnerAdapter;
import com.commit451.gitlab.adapter.MilestoneSpinnerAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.IssueChangedEvent;
import com.commit451.gitlab.event.IssueCreatedEvent;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Project;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Activity to input new issues, but not really a dialog at all wink wink
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

    private Project mProject;
    private Issue mIssue;
    private HashSet<Member> mMembers;

    private final Callback<List<Milestone>> mMilestonesCallback = new Callback<List<Milestone>>() {
        @Override
        public void onResponse(Response<List<Milestone>> response, Retrofit retrofit) {
            mMilestoneProgress.setVisibility(View.GONE);
            if (!response.isSuccess()) {
                mMilestoneSpinner.setVisibility(View.GONE);
                return;
            }
            mMilestoneSpinner.setVisibility(View.VISIBLE);
            MilestoneSpinnerAdapter milestoneSpinnerAdapter = new MilestoneSpinnerAdapter(AddIssueActivity.this, response.body());
            mMilestoneSpinner.setAdapter(milestoneSpinnerAdapter);
            if (mIssue != null) {
                mMilestoneSpinner.setSelection(milestoneSpinnerAdapter.getSelectedItemPosition(mIssue.getMilestone()));
            }
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
            if (!response.isSuccess()) {
                mAssigneeProgress.setVisibility(View.GONE);
                mAssigneeSpinner.setVisibility(View.GONE);
                return;
            }
            if (response.body() != null) {
                mMembers.addAll(response.body());
            }
            if (mProject.belongsToGroup()) {
                Timber.d("Project belongs to a group, loading those users too");
                GitLabClient.instance().getGroupMembers(mProject.getNamespace().getId()).enqueue(mGroupMembersCallback);
            } else {
                setAssignees();
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mAssigneeSpinner.setVisibility(View.GONE);
            mAssigneeProgress.setVisibility(View.GONE);
        }
    };

    private final Callback<List<Member>> mGroupMembersCallback = new Callback<List<Member>>() {
        @Override
        public void onResponse(Response<List<Member>> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                mAssigneeSpinner.setVisibility(View.GONE);
                return;
            }
            if (response.body() != null) {
                mMembers.addAll(response.body());
            }
            setAssignees();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mAssigneeSpinner.setVisibility(View.GONE);
            mAssigneeProgress.setVisibility(View.GONE);
        }
    };

    private final Callback<Issue> mIssueCreatedCallback = new Callback<Issue>() {

        @Override
        public void onResponse(Response<Issue> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                Snackbar.make(mRoot, getString(R.string.failed_to_create_issue), Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            if (mIssue == null) {
                LabCoatApp.bus().post(new IssueCreatedEvent(response.body()));
            } else {
                LabCoatApp.bus().post(new IssueChangedEvent(response.body()));
            }
            dismiss();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
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
        mMembers = new HashSet<>();

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

    private void setAssignees() {
        mAssigneeProgress.setVisibility(View.GONE);
        mAssigneeSpinner.setVisibility(View.VISIBLE);
        AssigneeSpinnerAdapter assigneeSpinnerAdapter = new AssigneeSpinnerAdapter(this, new ArrayList<>(mMembers));
        mAssigneeSpinner.setAdapter(assigneeSpinnerAdapter);
        if (mIssue != null) {
            mAssigneeSpinner.setSelection(assigneeSpinnerAdapter.getSelectedItemPosition(mIssue.getAssignee()));
        }
    }

    private void save() {
        if(!TextUtils.isEmpty(mTitleInput.getText())) {
            mTitleInputLayout.setError(null);
            showLoading();
            Long assigneeId = null;
            if (mAssigneeSpinner.getAdapter() != null ) {
                //the user did make a selection of some sort. So update it
                Member member = (Member) mAssigneeSpinner.getSelectedItem();
                if (member == null) {
                    //Removes the assignment
                    assigneeId = 0L;
                } else {
                    assigneeId = member.getId();
                }
            }

            Long milestoneId = null;
            if (mMilestoneSpinner.getAdapter() != null) {
                //the user did make a selection of some sort. So update it
                Milestone milestone = (Milestone) mMilestoneSpinner.getSelectedItem();
                if (milestone == null) {
                    //Removes the assignment
                    milestoneId = 0L;
                } else {
                    milestoneId = milestone.getId();
                }
            }
            createOrSaveIssue(mTitleInput.getText().toString(),
                    mDescriptionInput.getText().toString(),
                    assigneeId,
                    milestoneId);
        }
        else {
            mTitleInputLayout.setError(getString(R.string.required_field));
        }
    }

    private void createOrSaveIssue(String title, String description, Long assigneeId, Long milestoneId) {
        if (mIssue == null) {
            GitLabClient.instance().createIssue(
                    mProject.getId(),
                    title,
                    description,
                    assigneeId,
                    milestoneId).enqueue(mIssueCreatedCallback);
        } else {
            GitLabClient.instance().updateIssue(mProject.getId(),
                    mIssue.getId(),
                    title,
                    description,
                    assigneeId,
                    milestoneId).enqueue(mIssueCreatedCallback);
        }
    }

}