package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.commit451.adapterflowlayout.AdapterFlowLayout;
import com.commit451.easycallback.EasyCallback;
import com.commit451.easycallback.NullBodyException;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.AddIssueLabelAdapter;
import com.commit451.gitlab.adapter.AssigneeSpinnerAdapter;
import com.commit451.gitlab.adapter.MilestoneSpinnerAdapter;
import com.commit451.gitlab.event.IssueChangedEvent;
import com.commit451.gitlab.event.IssueCreatedEvent;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Label;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.teleprinter.Teleprinter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Callback;
import timber.log.Timber;

/**
 * Activity to input new issues, but not really a dialog at all wink wink
 */
public class AddIssueActivity extends MorphActivity {

    private static final int REQUEST_LABEL = 1;
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

    @BindView(R.id.root)
    FrameLayout mRoot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.title_text_input_layout)
    TextInputLayout mTitleInputLayout;
    @BindView(R.id.title)
    EditText mTitleInput;
    @BindView(R.id.description)
    EditText mDescriptionInput;
    @BindView(R.id.progress)
    View mProgress;
    @BindView(R.id.assignee_progress)
    View mAssigneeProgress;
    @BindView(R.id.assignee_spinner)
    Spinner mAssigneeSpinner;
    @BindView(R.id.milestone_progress)
    View mMilestoneProgress;
    @BindView(R.id.milestone_spinner)
    Spinner mMilestoneSpinner;
    @BindView(R.id.label_label)
    TextView mLabelLabel;
    @BindView(R.id.labels_progress)
    View mLabelsProgress;
    @BindView(R.id.root_add_labels)
    ViewGroup mRootAddLabels;
    @BindView(R.id.list_labels)
    AdapterFlowLayout mListLabels;

    AddIssueLabelAdapter mLabelsAdapter;
    Teleprinter mTeleprinter;

    Project mProject;
    Issue mIssue;
    HashSet<Member> mMembers;

    private final Callback<List<Milestone>> mMilestonesCallback = new EasyCallback<List<Milestone>>() {
        @Override
        public void success(@NonNull List<Milestone> response) {
            mMilestoneProgress.setVisibility(View.GONE);
            mMilestoneSpinner.setVisibility(View.VISIBLE);
            MilestoneSpinnerAdapter milestoneSpinnerAdapter = new MilestoneSpinnerAdapter(AddIssueActivity.this, response);
            mMilestoneSpinner.setAdapter(milestoneSpinnerAdapter);
            if (mIssue != null) {
                mMilestoneSpinner.setSelection(milestoneSpinnerAdapter.getSelectedItemPosition(mIssue.getMilestone()));
            }
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t);
            mMilestoneProgress.setVisibility(View.GONE);
            mMilestoneSpinner.setVisibility(View.GONE);
        }
    };

    private final Callback<List<Member>> mAssigneeCallback = new EasyCallback<List<Member>>() {
        @Override
        public void success(@NonNull List<Member> response) {
            mMembers.addAll(response);
            if (mProject.belongsToGroup()) {
                Timber.d("Project belongs to a group, loading those users too");
                App.instance().getGitLab().getGroupMembers(mProject.getNamespace().getId()).enqueue(mGroupMembersCallback);
            } else {
                setAssignees();
            }
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t);
            mAssigneeSpinner.setVisibility(View.GONE);
            mAssigneeProgress.setVisibility(View.GONE);
        }
    };

    private final Callback<List<Member>> mGroupMembersCallback = new EasyCallback<List<Member>>() {
        @Override
        public void success(@NonNull List<Member> response) {
            mMembers.addAll(response);
            setAssignees();
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t);
            mAssigneeSpinner.setVisibility(View.GONE);
            mAssigneeProgress.setVisibility(View.GONE);
        }
    };

    private final Callback<List<Label>> mLabelCallback = new EasyCallback<List<Label>>() {
        @Override
        public void success(@NonNull List<Label> response) {
            mLabelsProgress.setVisibility(View.GONE);
            mRootAddLabels.setVisibility(View.VISIBLE);
            setLabels(response);
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t);
            //null body could just mean no labels have been created for this project
            if (t instanceof NullBodyException) {
                setLabels(new ArrayList<Label>());
            } else {
                mListLabels.setVisibility(View.GONE);
                mLabelsProgress.setVisibility(View.GONE);
                mLabelLabel.setVisibility(View.GONE);
            }
        }
    };

    private final Callback<Issue> mIssueCreatedCallback = new EasyCallback<Issue>() {

        @Override
        public void success(@NonNull Issue response) {
            if (mIssue == null) {
                App.bus().post(new IssueCreatedEvent(response));
            } else {
                App.bus().post(new IssueChangedEvent(response));
            }
            dismiss();
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t);
            Snackbar.make(mRoot, getString(R.string.failed_to_create_issue), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    @OnClick(R.id.text_add_labels)
    void onAddLabelClicked() {
        Navigator.navigateToAddLabels(this, mProject, REQUEST_LABEL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_issue);
        ButterKnife.bind(this);
        morph(mRoot);
        mTeleprinter = new Teleprinter(this);

        mProject = Parcels.unwrap(getIntent().getParcelableExtra(KEY_PROJECT));
        mIssue = Parcels.unwrap(getIntent().getParcelableExtra(KEY_ISSUE));
        mMembers = new HashSet<>();
        mLabelsAdapter = new AddIssueLabelAdapter(new AddIssueLabelAdapter.Listener() {
            @Override
            public void onLabelLongClicked(final Label label) {
                new AlertDialog.Builder(AddIssueActivity.this)
                        .setTitle(R.string.remove)
                        .setMessage(R.string.are_you_sure_you_want_to_remove)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mLabelsAdapter.removeLabel(label);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        mListLabels.setAdapter(mLabelsAdapter);

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
        App.instance().getGitLab().getMilestones(mProject.getId(), getString(R.string.milestone_state_value_default)).enqueue(mMilestonesCallback);
        App.instance().getGitLab().getProjectMembers(mProject.getId()).enqueue(mAssigneeCallback);
        App.instance().getGitLab().getLabels(mProject.getId()).enqueue(mLabelCallback);
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

    private void setLabels(List<Label> projectLabels) {
        if (projectLabels != null && !projectLabels.isEmpty() && mIssue != null && mIssue.getLabels() != null) {
            ArrayList<Label> currentLabels = new ArrayList<>();
            for (Label label : projectLabels) {
                for (String labelName : mIssue.getLabels()) {
                    if (labelName.equals(label.getName())) {
                        currentLabels.add(label);
                    }
                }
            }
            if (!currentLabels.isEmpty()) {
                mLabelsAdapter.setLabels(currentLabels);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_LABEL:
                if (resultCode == RESULT_OK) {
                    Label label = Parcels.unwrap(data.getParcelableExtra(AddLabelActivity.KEY_LABEL));
                    if (mLabelsAdapter.containsLabel(label)) {
                        Snackbar.make(mRoot, R.string.label_already_added, Snackbar.LENGTH_SHORT)
                                .show();
                    } else {
                        mLabelsAdapter.addLabel(label);
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.discard)
                .setMessage(R.string.are_you_sure_you_want_to_discard)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void save() {
        if (!TextUtils.isEmpty(mTitleInput.getText())) {
            mTeleprinter.hideKeyboard();
            mTitleInputLayout.setError(null);
            showLoading();
            Long assigneeId = null;
            if (mAssigneeSpinner.getAdapter() != null) {
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
            String labelsCommaSeperated = mLabelsAdapter.getCommaSeperatedStringOfLabels();
            createOrSaveIssue(mTitleInput.getText().toString(),
                    mDescriptionInput.getText().toString(),
                    assigneeId,
                    milestoneId,
                    labelsCommaSeperated);
        } else {
            mTitleInputLayout.setError(getString(R.string.required_field));
        }
    }

    private void createOrSaveIssue(String title, String description, @Nullable Long assigneeId,
                                   @Nullable Long milestoneId, @Nullable String labels) {
        if (mIssue == null) {
            App.instance().getGitLab().createIssue(
                    mProject.getId(),
                    title,
                    description,
                    assigneeId,
                    milestoneId,
                    labels).enqueue(mIssueCreatedCallback);
        } else {
            App.instance().getGitLab().updateIssue(mProject.getId(),
                    mIssue.getId(),
                    title,
                    description,
                    assigneeId,
                    milestoneId,
                    labels).enqueue(mIssueCreatedCallback);
        }
    }

}