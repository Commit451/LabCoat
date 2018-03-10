package com.commit451.gitlab.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.commit451.adapterflowlayout.AdapterFlowLayout
import com.commit451.addendum.parceler.getParcelerParcelableExtra
import com.commit451.addendum.parceler.putParcelerParcelableExtra
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.AddIssueLabelAdapter
import com.commit451.gitlab.adapter.AssigneeSpinnerAdapter
import com.commit451.gitlab.adapter.MilestoneSpinnerAdapter
import com.commit451.gitlab.event.IssueChangedEvent
import com.commit451.gitlab.event.IssueCreatedEvent
import com.commit451.gitlab.extension.belongsToGroup
import com.commit451.gitlab.extension.checkValid
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.*
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.teleprinter.Teleprinter
import io.reactivex.Single
import timber.log.Timber
import java.util.*

/**
 * Activity to input new issues, but not really a dialog at all wink wink
 */
class AddIssueActivity : MorphActivity() {

    companion object {

        private val REQUEST_LABEL = 1
        private val KEY_PROJECT = "project"
        private val KEY_ISSUE = "issue"

        fun newIntent(context: Context, project: Project, issue: Issue?): Intent {
            val intent = Intent(context, AddIssueActivity::class.java)
            intent.putParcelerParcelableExtra(KEY_PROJECT, project)
            if (issue != null) {
                intent.putParcelerParcelableExtra(KEY_ISSUE, issue)
            }
            return intent
        }
    }

    @BindView(R.id.root)
    lateinit var root: ViewGroup
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.title_text_input_layout)
    lateinit var textInputLayoutTitle: TextInputLayout
    @BindView(R.id.description)
    lateinit var textDescription: EditText
    @BindView(R.id.progress)
    lateinit var progress: View
    @BindView(R.id.assignee_progress)
    lateinit var progressAssignee: View
    @BindView(R.id.assignee_spinner)
    lateinit var spinnerAssignee: Spinner
    @BindView(R.id.milestone_progress)
    lateinit var progressMilestone: View
    @BindView(R.id.milestone_spinner)
    lateinit var spinnerMilestone: Spinner
    @BindView(R.id.label_label)
    lateinit var textLabel: TextView
    @BindView(R.id.labels_progress)
    lateinit var progressLabels: View
    @BindView(R.id.root_add_labels)
    lateinit var rootAddLabels: ViewGroup
    @BindView(R.id.list_labels)
    lateinit var listLabels: AdapterFlowLayout
    @BindView(R.id.check_confidential)
    lateinit var checkConfidential: CheckBox

    lateinit var adapterLabels: AddIssueLabelAdapter
    lateinit var teleprinter: Teleprinter

    lateinit var project: Project
    var issue: Issue? = null
    lateinit var members: HashSet<User>

    @OnClick(R.id.text_add_labels)
    fun onAddLabelClicked() {
        Navigator.navigateToAddLabels(this, project, REQUEST_LABEL)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_issue)
        ButterKnife.bind(this)
        morph(root)
        teleprinter = Teleprinter(this)

        project = intent.getParcelerParcelableExtra<Project>(KEY_PROJECT)!!
        issue = intent.getParcelerParcelableExtra<Issue>(KEY_ISSUE)
        members = HashSet<User>()
        adapterLabels = AddIssueLabelAdapter(object : AddIssueLabelAdapter.Listener {
            override fun onLabelClicked(label: Label) {
                AlertDialog.Builder(this@AddIssueActivity)
                        .setTitle(R.string.remove)
                        .setMessage(R.string.are_you_sure_you_want_to_remove)
                        .setPositiveButton(R.string.yes) { _, _ -> adapterLabels.removeLabel(label) }
                        .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                        .show()
            }
        })
        listLabels.adapter = adapterLabels

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_create, R.id.action_edit -> {
                    save()
                    return@OnMenuItemClickListener true
                }
            }
            false
        })

        if (issue != null) {
            bindIssue()
            toolbar.inflateMenu(R.menu.edit)
        } else {
            toolbar.inflateMenu(R.menu.create)
        }
        load()
    }

    private fun load() {
        App.get().gitLab.getMilestones(project.id, getString(R.string.milestone_state_value_default))
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Milestone>>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        progressMilestone.visibility = View.GONE
                        spinnerMilestone.visibility = View.GONE
                    }

                    override fun responseNonNullSuccess(milestones: List<Milestone>) {
                        progressMilestone.visibility = View.GONE
                        spinnerMilestone.visibility = View.VISIBLE
                        val maybeNullMilestones = mutableListOf<Milestone?>()
                        maybeNullMilestones.addAll(milestones)
                        val milestoneSpinnerAdapter = MilestoneSpinnerAdapter(this@AddIssueActivity, maybeNullMilestones)
                        spinnerMilestone.adapter = milestoneSpinnerAdapter
                        if (issue != null) {
                            spinnerMilestone.setSelection(milestoneSpinnerAdapter.getSelectedItemPosition(issue!!.milestone))
                        }
                    }
                })
        App.get().gitLab.getProjectMembers(project.id)
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<User>>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        spinnerAssignee.visibility = View.GONE
                        progressAssignee.visibility = View.GONE
                    }

                    override fun responseNonNullSuccess(members: List<User>) {
                        this@AddIssueActivity.members.addAll(members)
                        if (project.belongsToGroup()) {
                            Timber.d("Project belongs to a group, loading those users too")
                            App.get().gitLab.getGroupMembers(project.namespace.id)
                                    .with(this@AddIssueActivity)
                                    .subscribe(object : CustomResponseSingleObserver<List<User>>() {

                                        override fun error(t: Throwable) {
                                            Timber.e(t)
                                            spinnerAssignee.visibility = View.GONE
                                            progressAssignee.visibility = View.GONE
                                        }

                                        override fun responseNonNullSuccess(members: List<User>) {
                                            this@AddIssueActivity.members.addAll(members)
                                            setAssignees()
                                        }
                                    })
                        } else {
                            setAssignees()
                        }
                    }
                })
        App.get().gitLab.getLabels(project.id)
                .with(this)
                .subscribe(object : CustomSingleObserver<List<Label>>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        listLabels.visibility = View.GONE
                        progressLabels.visibility = View.GONE
                        textLabel.visibility = View.GONE
                    }

                    override fun success(labels: List<Label>) {
                        progressLabels.visibility = View.GONE
                        rootAddLabels.visibility = View.VISIBLE
                        setLabels(labels)
                    }
                })
    }

    private fun showLoading() {
        progress.visibility = View.VISIBLE
        progress.alpha = 0.0f
        progress.animate().alpha(1.0f)
    }

    private fun bindIssue() {
        if (!issue?.title.isNullOrEmpty()) {
            textInputLayoutTitle.editText!!.setText(issue!!.title)
        }
        if (!issue?.description.isNullOrEmpty()) {
            textDescription.setText(issue!!.description)
        }
        checkConfidential.isChecked = issue!!.isConfidential
    }

    private fun setAssignees() {
        progressAssignee.visibility = View.GONE
        spinnerAssignee.visibility = View.VISIBLE
        val assigneeSpinnerAdapter = AssigneeSpinnerAdapter(this, ArrayList(members))
        spinnerAssignee.adapter = assigneeSpinnerAdapter
        if (issue != null) {
            spinnerAssignee.setSelection(assigneeSpinnerAdapter.getSelectedItemPosition(issue!!.assignee))
        }
    }

    private fun setLabels(projectLabels: List<Label>?) {
        if (projectLabels != null && !projectLabels.isEmpty() && issue != null && issue!!.labels != null) {
            val currentLabels = ArrayList<Label>()
            for (label in projectLabels) {
                for (labelName in issue!!.labels!!) {
                    if (labelName == label.name) {
                        currentLabels.add(label)
                    }
                }
            }
            if (!currentLabels.isEmpty()) {
                adapterLabels.setLabels(currentLabels)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_LABEL ->
                if (resultCode == Activity.RESULT_OK) {
                    val label = data?.getParcelerParcelableExtra<Label>(AddLabelActivity.KEY_LABEL)!!
                    if (adapterLabels.containsLabel(label)) {
                        Snackbar.make(root, R.string.label_already_added, Snackbar.LENGTH_SHORT)
                                .show()
                    } else {
                        adapterLabels.addLabel(label)
                    }
                }
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
                .setTitle(R.string.discard)
                .setMessage(R.string.are_you_sure_you_want_to_discard)
                .setPositiveButton(R.string.yes) { _, _ -> dismiss() }
                .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun save() {
        if (textInputLayoutTitle.checkValid()) {
            teleprinter.hideKeyboard()
            showLoading()
            var assigneeId: Long? = null
            if (spinnerAssignee.adapter != null) {
                //the user did make a selection of some sort. So update it
                val member = spinnerAssignee.selectedItem as? User?
                if (member == null) {
                    //Removes the assignment
                    assigneeId = 0L
                } else {
                    assigneeId = member.id
                }
            }

            var milestoneId: Long? = null
            if (spinnerMilestone.adapter != null) {
                //the user did make a selection of some sort. So update it
                val milestone = spinnerMilestone.selectedItem as? Milestone?
                if (milestone == null) {
                    //Removes the assignment
                    milestoneId = 0L
                } else {
                    milestoneId = milestone.id
                }
            }
            val labelsCommaSeperated = adapterLabels.getCommaSeperatedStringOfLabels()
            createOrSaveIssue(textInputLayoutTitle.editText!!.text.toString(),
                    textDescription.text.toString(),
                    assigneeId,
                    milestoneId,
                    labelsCommaSeperated,
                    checkConfidential.isChecked)
        }
    }

    private fun createOrSaveIssue(title: String, description: String, assigneeId: Long?,
                                  milestoneId: Long?, labels: String?, isConfidential: Boolean) {
        if (issue == null) {
            observeUpdate(App.get().gitLab.createIssue(
                    project.id,
                    title,
                    description,
                    assigneeId,
                    milestoneId,
                    labels,
                    isConfidential))
        } else {
            observeUpdate(App.get().gitLab.updateIssue(project.id,
                    issue!!.iid,
                    title,
                    description,
                    assigneeId,
                    milestoneId,
                    labels,
                    isConfidential))
        }
    }

    private fun observeUpdate(observable: Single<Issue>) {
        observable.with(this)
                .subscribe(object : CustomSingleObserver<Issue>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        Snackbar.make(root, getString(R.string.failed_to_create_issue), Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun success(issue: Issue) {
                        if (this@AddIssueActivity.issue == null) {
                            App.bus().post(IssueCreatedEvent(issue))
                        } else {
                            App.bus().post(IssueChangedEvent(issue))
                        }
                        dismiss()
                    }
                })
    }
}