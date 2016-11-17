package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
        App.get().getGitLab().getMilestones(mProject.getId(), getString(R.string.milestone_state_value_default))
                .compose(this.<Response<List<Milestone>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<List<Milestone>>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mMilestoneProgress.setVisibility(View.GONE);
                        mMilestoneSpinner.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(Response<List<Milestone>> listResponse) {
                        if (!listResponse.isSuccessful()) {
                            onError(new HttpException(listResponse));
                            return;
                        }
                        mMilestoneProgress.setVisibility(View.GONE);
                        mMilestoneSpinner.setVisibility(View.VISIBLE);
                        MilestoneSpinnerAdapter milestoneSpinnerAdapter = new MilestoneSpinnerAdapter(AddIssueActivity.this, listResponse.body());
                        mMilestoneSpinner.setAdapter(milestoneSpinnerAdapter);
                        if (mIssue != null) {
                            mMilestoneSpinner.setSelection(milestoneSpinnerAdapter.getSelectedItemPosition(mIssue.getMilestone()));
                        }
                    }
                });
        App.get().getGitLab().getProjectMembers(mProject.getId())
                .compose(this.<Response<List<Member>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<List<Member>>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mAssigneeSpinner.setVisibility(View.GONE);
                        mAssigneeProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(Response<List<Member>> listResponse) {
                        if (!listResponse.isSuccessful()) {
                            onError(new HttpException(listResponse));
                            return;
                        }
                        mMembers.addAll(listResponse.body());
                        if (mProject.belongsToGroup()) {
                            Timber.d("Project belongs to a group, loading those users too");
                            App.get().getGitLab().getGroupMembers(mProject.getNamespace().getId())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Subscriber<Response<List<Member>>>() {
                                        @Override
                                        public void onCompleted() {
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Timber.e(e);
                                            mAssigneeSpinner.setVisibility(View.GONE);
                                            mAssigneeProgress.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onNext(Response<List<Member>> listResponse) {
                                            if (!listResponse.isSuccessful()) {
                                                onError(new HttpException(listResponse));
                                                return;
                                            }
                                            mMembers.addAll(listResponse.body());
                                            setAssignees();
                                        }
                                    });
                        } else {
                            setAssignees();
                        }
                    }
                });
        App.get().getGitLab().getLabels(mProject.getId())
                .compose(this.<List<Label>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Label>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mListLabels.setVisibility(View.GONE);
                        mLabelsProgress.setVisibility(View.GONE);
                        mLabelLabel.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(List<Label> labels) {
                        mLabelsProgress.setVisibility(View.GONE);
                        mRootAddLabels.setVisibility(View.VISIBLE);
                        setLabels(labels);
                    }
                });
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
            observeUpdate(App.get().getGitLab().createIssue(
                    mProject.getId(),
                    title,
                    description,
                    assigneeId,
                    milestoneId,
                    labels));
        } else {
            observeUpdate(App.get().getGitLab().updateIssue(mProject.getId(),
                    mIssue.getId(),
                    title,
                    description,
                    assigneeId,
                    milestoneId,
                    labels));
        }
    }

    private void observeUpdate(Observable<Issue> observable) {
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Issue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        Snackbar.make(mRoot, getString(R.string.failed_to_create_issue), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onNext(Issue issue) {
                        if (mIssue == null) {
                            App.bus().post(new IssueCreatedEvent(issue));
                        } else {
                            App.bus().post(new IssueChangedEvent(issue));
                        }
                        dismiss();
                    }
                });
    }

}