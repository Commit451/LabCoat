package com.commit451.gitlab.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.commit451.adapterflowlayout.AdapterFlowLayout
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.AddIssueLabelAdapter
import com.commit451.gitlab.adapter.AssigneeSpinnerAdapter
import com.commit451.gitlab.adapter.MilestoneSpinnerAdapter
import com.commit451.gitlab.event.IssueChangedEvent
import com.commit451.gitlab.event.IssueCreatedEvent
import com.commit451.gitlab.model.api.*
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.teleprinter.Teleprinter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.parceler.Parcels
import retrofit2.Response
import timber.log.Timber
import java.util.*

/**
 * Activity to input new issues, but not really a dialog at all wink wink
 */
class AddIssueActivity : MorphActivity() {

    @BindView(R.id.root)
    lateinit var root: FrameLayout
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

    lateinit var adapterLabels: AddIssueLabelAdapter
    lateinit var teleprinter: Teleprinter

    lateinit var project: Project
    var issue: Issue? = null
    lateinit var members: HashSet<Member>

    @OnClick(R.id.text_add_labels)
    internal fun onAddLabelClicked() {
        Navigator.navigateToAddLabels(this, project, REQUEST_LABEL)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_issue)
        ButterKnife.bind(this)
        morph(root)
        teleprinter = Teleprinter(this)

        project = Parcels.unwrap<Project>(intent.getParcelableExtra<Parcelable>(KEY_PROJECT))
        issue = Parcels.unwrap<Issue>(intent.getParcelableExtra<Parcelable>(KEY_ISSUE))
        members = HashSet<Member>()
        adapterLabels = AddIssueLabelAdapter(AddIssueLabelAdapter.Listener { label ->
            AlertDialog.Builder(this@AddIssueActivity)
                    .setTitle(R.string.remove)
                    .setMessage(R.string.are_you_sure_you_want_to_remove)
                    .setPositiveButton(android.R.string.yes) { dialog, which -> adapterLabels.removeLabel(label) }
                    .setNegativeButton(android.R.string.no) { dialog, which -> dialog.dismiss() }
                    .show()
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
            toolbar.inflateMenu(R.menu.menu_edit_milestone)
        } else {
            toolbar.inflateMenu(R.menu.menu_add_milestone)
        }
        load()
    }

    private fun load() {
        App.get().gitLab.getMilestones(project.id, getString(R.string.milestone_state_value_default))
                .compose(this.bindToLifecycle<Response<List<Milestone>>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<List<Milestone>>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        progressMilestone.visibility = View.GONE
                        spinnerMilestone.visibility = View.GONE
                    }

                    override fun responseSuccess(milestones: List<Milestone>) {
                        progressMilestone.visibility = View.GONE
                        spinnerMilestone.visibility = View.VISIBLE
                        val milestoneSpinnerAdapter = MilestoneSpinnerAdapter(this@AddIssueActivity, milestones)
                        spinnerMilestone.adapter = milestoneSpinnerAdapter
                        if (issue != null) {
                            spinnerMilestone.setSelection(milestoneSpinnerAdapter.getSelectedItemPosition(issue!!.milestone))
                        }
                    }
                })
        App.get().gitLab.getProjectMembers(project.id)
                .compose(this.bindToLifecycle<Response<List<Member>>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<List<Member>>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        spinnerAssignee.visibility = View.GONE
                        progressAssignee.visibility = View.GONE
                    }

                    override fun responseSuccess(members: List<Member>) {
                        this@AddIssueActivity.members.addAll(members)
                        if (project.belongsToGroup()) {
                            Timber.d("Project belongs to a group, loading those users too")
                            App.get().gitLab.getGroupMembers(project.namespace.id)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : CustomResponseSingleObserver<List<Member>>() {

                                        override fun error(t: Throwable) {
                                            Timber.e(t)
                                            spinnerAssignee.visibility = View.GONE
                                            progressAssignee.visibility = View.GONE
                                        }

                                        override fun responseSuccess(members: List<Member>) {
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
                .compose(this.bindToLifecycle<List<Label>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
        if (!TextUtils.isEmpty(issue!!.title)) {
            textInputLayoutTitle.editText!!.setText(issue!!.title)
        }
        if (!TextUtils.isEmpty(issue!!.description)) {
            textDescription.setText(issue!!.description)
        }
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
                for (labelName in issue!!.labels) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_LABEL -> if (resultCode == Activity.RESULT_OK) {
                val label = Parcels.unwrap<Label>(data.getParcelableExtra<Parcelable>(AddLabelActivity.KEY_LABEL))
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
                .setPositiveButton(android.R.string.yes) { dialog, which -> dismiss() }
                .setNegativeButton(android.R.string.no) { dialog, which -> dialog.dismiss() }
                .show()
    }

    private fun save() {
        if (!TextUtils.isEmpty(textInputLayoutTitle.editText!!.text)) {
            teleprinter.hideKeyboard()
            textInputLayoutTitle.error = null
            showLoading()
            var assigneeId: Long? = null
            if (spinnerAssignee.adapter != null) {
                //the user did make a selection of some sort. So update it
                val member = spinnerAssignee.selectedItem as? Member?
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
            val labelsCommaSeperated = adapterLabels.commaSeperatedStringOfLabels
            createOrSaveIssue(textInputLayoutTitle.editText!!.text.toString(),
                    textDescription.text.toString(),
                    assigneeId,
                    milestoneId,
                    labelsCommaSeperated)
        } else {
            textInputLayoutTitle.error = getString(R.string.required_field)
        }
    }

    private fun createOrSaveIssue(title: String, description: String, assigneeId: Long?,
                                  milestoneId: Long?, labels: String?) {
        if (issue == null) {
            observeUpdate(App.get().gitLab.createIssue(
                    project.id,
                    title,
                    description,
                    assigneeId,
                    milestoneId,
                    labels))
        } else {
            observeUpdate(App.get().gitLab.updateIssue(project.id,
                    issue!!.id,
                    title,
                    description,
                    assigneeId,
                    milestoneId,
                    labels))
        }
    }

    private fun observeUpdate(observable: Single<Issue>) {
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomSingleObserver<Issue>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        Snackbar.make(root!!, getString(R.string.failed_to_create_issue), Snackbar.LENGTH_SHORT)
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

    companion object {

        private val REQUEST_LABEL = 1
        private val KEY_PROJECT = "project"
        private val KEY_ISSUE = "issue"

        fun newIntent(context: Context, project: Project, issue: Issue?): Intent {
            val intent = Intent(context, AddIssueActivity::class.java)
            intent.putExtra(KEY_PROJECT, Parcels.wrap(project))
            if (issue != null) {
                intent.putExtra(KEY_ISSUE, Parcels.wrap(issue))
            }
            return intent
        }
    }

}