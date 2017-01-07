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
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.teleprinter.Teleprinter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
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
    FrameLayout root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_text_input_layout)
    TextInputLayout textInputLayoutTitle;
    @BindView(R.id.description)
    EditText textDescription;
    @BindView(R.id.progress)
    View progress;
    @BindView(R.id.assignee_progress)
    View progressAssignee;
    @BindView(R.id.assignee_spinner)
    Spinner spinnerAssignee;
    @BindView(R.id.milestone_progress)
    View progressMilestone;
    @BindView(R.id.milestone_spinner)
    Spinner spinnerMilestone;
    @BindView(R.id.label_label)
    TextView textLabel;
    @BindView(R.id.labels_progress)
    View progressLabels;
    @BindView(R.id.root_add_labels)
    ViewGroup rootAddLabels;
    @BindView(R.id.list_labels)
    AdapterFlowLayout listLabels;

    AddIssueLabelAdapter adapterLabels;
    Teleprinter teleprinter;

    Project project;
    Issue issue;
    HashSet<Member> members;

    @OnClick(R.id.text_add_labels)
    void onAddLabelClicked() {
        Navigator.navigateToAddLabels(this, project, REQUEST_LABEL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_issue);
        ButterKnife.bind(this);
        morph(root);
        teleprinter = new Teleprinter(this);

        project = Parcels.unwrap(getIntent().getParcelableExtra(KEY_PROJECT));
        issue = Parcels.unwrap(getIntent().getParcelableExtra(KEY_ISSUE));
        members = new HashSet<>();
        adapterLabels = new AddIssueLabelAdapter(new AddIssueLabelAdapter.Listener() {
            @Override
            public void onLabelLongClicked(final Label label) {
                new AlertDialog.Builder(AddIssueActivity.this)
                        .setTitle(R.string.remove)
                        .setMessage(R.string.are_you_sure_you_want_to_remove)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapterLabels.removeLabel(label);
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
        listLabels.setAdapter(adapterLabels);

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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

        if (issue != null) {
            bindIssue();
            toolbar.inflateMenu(R.menu.menu_edit_milestone);
        } else {
            toolbar.inflateMenu(R.menu.menu_add_milestone);
        }
        load();
    }

    private void load() {
        App.get().getGitLab().getMilestones(project.getId(), getString(R.string.milestone_state_value_default))
                .compose(this.<Response<List<Milestone>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Milestone>>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        progressMilestone.setVisibility(View.GONE);
                        spinnerMilestone.setVisibility(View.GONE);
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Milestone> milestones) {
                        progressMilestone.setVisibility(View.GONE);
                        spinnerMilestone.setVisibility(View.VISIBLE);
                        MilestoneSpinnerAdapter milestoneSpinnerAdapter = new MilestoneSpinnerAdapter(AddIssueActivity.this, milestones);
                        spinnerMilestone.setAdapter(milestoneSpinnerAdapter);
                        if (issue != null) {
                            spinnerMilestone.setSelection(milestoneSpinnerAdapter.getSelectedItemPosition(issue.getMilestone()));
                        }
                    }
                });
        App.get().getGitLab().getProjectMembers(project.getId())
                .compose(this.<Response<List<Member>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Member>>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        spinnerAssignee.setVisibility(View.GONE);
                        progressAssignee.setVisibility(View.GONE);
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Member> members) {
                        AddIssueActivity.this.members.addAll(members);
                        if (project.belongsToGroup()) {
                            Timber.d("Project belongs to a group, loading those users too");
                            App.get().getGitLab().getGroupMembers(project.getNamespace().getId())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new CustomResponseSingleObserver<List<Member>>() {

                                        @Override
                                        public void error(@NonNull Throwable t) {
                                            Timber.e(t);
                                            spinnerAssignee.setVisibility(View.GONE);
                                            progressAssignee.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void responseSuccess(@NonNull List<Member> members) {
                                            AddIssueActivity.this.members.addAll(members);
                                            setAssignees();
                                        }
                                    });
                        } else {
                            setAssignees();
                        }
                    }
                });
        App.get().getGitLab().getLabels(project.getId())
                .compose(this.<List<Label>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<List<Label>>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        listLabels.setVisibility(View.GONE);
                        progressLabels.setVisibility(View.GONE);
                        textLabel.setVisibility(View.GONE);
                    }

                    @Override
                    public void success(@NonNull List<Label> labels) {
                        progressLabels.setVisibility(View.GONE);
                        rootAddLabels.setVisibility(View.VISIBLE);
                        setLabels(labels);
                    }
                });
    }

    private void showLoading() {
        progress.setVisibility(View.VISIBLE);
        progress.setAlpha(0.0f);
        progress.animate().alpha(1.0f);
    }

    private void bindIssue() {
        if (!TextUtils.isEmpty(issue.getTitle())) {
            textInputLayoutTitle.getEditText().setText(issue.getTitle());
        }
        if (!TextUtils.isEmpty(issue.getDescription())) {
            textDescription.setText(issue.getDescription());
        }
    }

    private void setAssignees() {
        progressAssignee.setVisibility(View.GONE);
        spinnerAssignee.setVisibility(View.VISIBLE);
        AssigneeSpinnerAdapter assigneeSpinnerAdapter = new AssigneeSpinnerAdapter(this, new ArrayList<>(members));
        spinnerAssignee.setAdapter(assigneeSpinnerAdapter);
        if (issue != null) {
            spinnerAssignee.setSelection(assigneeSpinnerAdapter.getSelectedItemPosition(issue.getAssignee()));
        }
    }

    private void setLabels(List<Label> projectLabels) {
        if (projectLabels != null && !projectLabels.isEmpty() && issue != null && issue.getLabels() != null) {
            ArrayList<Label> currentLabels = new ArrayList<>();
            for (Label label : projectLabels) {
                for (String labelName : issue.getLabels()) {
                    if (labelName.equals(label.getName())) {
                        currentLabels.add(label);
                    }
                }
            }
            if (!currentLabels.isEmpty()) {
                adapterLabels.setLabels(currentLabels);
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
                    if (adapterLabels.containsLabel(label)) {
                        Snackbar.make(root, R.string.label_already_added, Snackbar.LENGTH_SHORT)
                                .show();
                    } else {
                        adapterLabels.addLabel(label);
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
        if (!TextUtils.isEmpty(textInputLayoutTitle.getEditText().getText())) {
            teleprinter.hideKeyboard();
            textInputLayoutTitle.setError(null);
            showLoading();
            Long assigneeId = null;
            if (spinnerAssignee.getAdapter() != null) {
                //the user did make a selection of some sort. So update it
                Member member = (Member) spinnerAssignee.getSelectedItem();
                if (member == null) {
                    //Removes the assignment
                    assigneeId = 0L;
                } else {
                    assigneeId = member.getId();
                }
            }

            Long milestoneId = null;
            if (spinnerMilestone.getAdapter() != null) {
                //the user did make a selection of some sort. So update it
                Milestone milestone = (Milestone) spinnerMilestone.getSelectedItem();
                if (milestone == null) {
                    //Removes the assignment
                    milestoneId = 0L;
                } else {
                    milestoneId = milestone.getId();
                }
            }
            String labelsCommaSeperated = adapterLabels.getCommaSeperatedStringOfLabels();
            createOrSaveIssue(textInputLayoutTitle.getEditText().getText().toString(),
                    textDescription.getText().toString(),
                    assigneeId,
                    milestoneId,
                    labelsCommaSeperated);
        } else {
            textInputLayoutTitle.setError(getString(R.string.required_field));
        }
    }

    private void createOrSaveIssue(String title, String description, @Nullable Long assigneeId,
                                   @Nullable Long milestoneId, @Nullable String labels) {
        if (issue == null) {
            observeUpdate(App.get().getGitLab().createIssue(
                    project.getId(),
                    title,
                    description,
                    assigneeId,
                    milestoneId,
                    labels));
        } else {
            observeUpdate(App.get().getGitLab().updateIssue(project.getId(),
                    issue.getId(),
                    title,
                    description,
                    assigneeId,
                    milestoneId,
                    labels));
        }
    }

    private void observeUpdate(Single<Issue> observable) {
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Issue>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        Snackbar.make(root, getString(R.string.failed_to_create_issue), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void success(@NonNull Issue issue) {
                        if (AddIssueActivity.this.issue == null) {
                            App.bus().post(new IssueCreatedEvent(issue));
                        } else {
                            App.bus().post(new IssueChangedEvent(issue));
                        }
                        dismiss();
                    }
                });
    }

}