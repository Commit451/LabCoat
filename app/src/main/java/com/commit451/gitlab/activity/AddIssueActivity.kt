package com.commit451.gitlab.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.commit451.addendum.design.snackbar
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.AddIssueLabelAdapter
import com.commit451.gitlab.adapter.AssigneeSpinnerAdapter
import com.commit451.gitlab.adapter.MilestoneSpinnerAdapter
import com.commit451.gitlab.event.IssueChangedEvent
import com.commit451.gitlab.event.IssueCreatedEvent
import com.commit451.gitlab.extension.belongsToGroup
import com.commit451.gitlab.extension.checkValid
import com.commit451.gitlab.extension.mapResponseSuccess
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.*
import com.commit451.gitlab.navigation.Navigator
import com.commit451.teleprinter.Teleprinter
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.core.Single
import kotlinx.android.synthetic.main.activity_add_issue.*
import kotlinx.android.synthetic.main.progress_fullscreen.*
import timber.log.Timber
import java.util.*

/**
 * Activity to input new issues, but not really a dialog at all wink wink
 */
class AddIssueActivity : MorphActivity() {

    companion object {

        private const val REQUEST_LABEL = 1
        private const val KEY_PROJECT = "project"
        private const val KEY_ISSUE = "issue"

        fun newIntent(context: Context, project: Project, issue: Issue?): Intent {
            val intent = Intent(context, AddIssueActivity::class.java)
            intent.putExtra(KEY_PROJECT, project)
            if (issue != null) {
                intent.putExtra(KEY_ISSUE, issue)
            }
            return intent
        }
    }

    private lateinit var adapterLabels: AddIssueLabelAdapter
    private lateinit var teleprinter: Teleprinter

    private lateinit var project: Project
    private var issue: Issue? = null
    private lateinit var members: HashSet<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_issue)
        morph(root)
        teleprinter = Teleprinter(this)

        project = intent.getParcelableExtra(KEY_PROJECT)!!
        issue = intent.getParcelableExtra(KEY_ISSUE)
        members = HashSet()
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
        textAddLabels.setOnClickListener {
            Navigator.navigateToAddLabels(this, project, REQUEST_LABEL)
        }
        load()
    }

    private fun load() {
        App.get().gitLab.getMilestones(project.id, getString(R.string.milestone_state_value_default))
                .mapResponseSuccess()
                .with(this)
                .subscribe({
                    progressMilestone.visibility = View.GONE
                    spinnerMilestone.visibility = View.VISIBLE
                    val maybeNullMilestones = mutableListOf<Milestone?>()
                    maybeNullMilestones.addAll(it)
                    val milestoneSpinnerAdapter = MilestoneSpinnerAdapter(this@AddIssueActivity, maybeNullMilestones)
                    spinnerMilestone.adapter = milestoneSpinnerAdapter
                    if (issue != null) {
                        spinnerMilestone.setSelection(milestoneSpinnerAdapter.getSelectedItemPosition(issue!!.milestone))
                    }
                }, {
                    Timber.e(it)
                    progressMilestone.visibility = View.GONE
                    spinnerMilestone.visibility = View.GONE
                })
        App.get().gitLab.getProjectMembers(project.id)
                .mapResponseSuccess()
                .with(this)
                .subscribe({
                    this.members.addAll(members)
                    if (project.belongsToGroup()) {
                        Timber.d("Project belongs to a group, loading those users too")
                        App.get().gitLab.getGroupMembers(project.namespace!!.id)
                                .mapResponseSuccess()
                                .with(this@AddIssueActivity)
                                .subscribe( {
                                    this.members.addAll(it)
                                    setAssignees()
                                }, {
                                    Timber.e(it)
                                    spinnerAssignee.visibility = View.GONE
                                    progressAssignee.visibility = View.GONE
                                })
                    } else {
                        setAssignees()
                    }
                }, {
                    Timber.e(it)
                    spinnerAssignee.visibility = View.GONE
                    progressAssignee.visibility = View.GONE
                })
        App.get().gitLab.getLabels(project.id)
                .with(this)
                .subscribe({
                    progressLabels.visibility = View.GONE
                    rootAddLabels.visibility = View.VISIBLE
                    setLabels(it)
                }, {
                    Timber.e(it)
                    listLabels.visibility = View.GONE
                    progressLabels.visibility = View.GONE
                    textLabel.visibility = View.GONE
                })
    }

    private fun showLoading() {
        fullscreenProgress.visibility = View.VISIBLE
        fullscreenProgress.alpha = 0.0f
        fullscreenProgress.animate().alpha(1.0f)
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
        if (projectLabels != null && projectLabels.isNotEmpty() && issue != null && issue!!.labels != null) {
            val currentLabels = ArrayList<Label>()
            for (label in projectLabels) {
                for (labelName in issue!!.labels!!) {
                    if (labelName == label.name) {
                        currentLabels.add(label)
                    }
                }
            }
            if (currentLabels.isNotEmpty()) {
                adapterLabels.setLabels(currentLabels)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_LABEL ->
                if (resultCode == Activity.RESULT_OK) {
                    val label = data?.getParcelableExtra<Label>(AddLabelActivity.KEY_LABEL)!!
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
                assigneeId = member?.id ?: //Removes the assignment
                        0L
            }

            var milestoneId: Long? = null
            if (spinnerMilestone.adapter != null) {
                //the user did make a selection of some sort. So update it
                val milestone = spinnerMilestone.selectedItem as? Milestone?
                milestoneId = milestone?.id ?: //Removes the assignment
                        0L
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

    private fun observeUpdate(single: Single<Issue>) {
        single.with(this)
                .subscribe({
                    if (issue == null) {
                        App.bus().post(IssueCreatedEvent(it))
                    } else {
                        App.bus().post(IssueChangedEvent(it))
                    }
                    dismiss()
                }, {
                    Timber.e(it)
                    root.snackbar(R.string.failed_to_create_issue)
                })
    }
}
