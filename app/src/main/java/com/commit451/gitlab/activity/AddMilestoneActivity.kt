package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.commit451.addendum.design.snackbar
import com.commit451.addendum.themeAttrColor
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.api.converter.DashDateAdapter
import com.commit451.gitlab.event.MilestoneChangedEvent
import com.commit451.gitlab.event.MilestoneCreatedEvent
import com.commit451.gitlab.extension.checkValid
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Milestone
import com.commit451.teleprinter.Teleprinter
import com.google.android.material.snackbar.Snackbar
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_add_milestone.*
import kotlinx.android.synthetic.main.progress_fullscreen.*
import timber.log.Timber
import java.util.*

class AddMilestoneActivity : MorphActivity() {

    companion object {

        private const val KEY_PROJECT_ID = "project_id"
        private const val KEY_MILESTONE = "milestone"

        @JvmOverloads
        fun newIntent(context: Context, projectId: Long, milestone: Milestone? = null): Intent {
            val intent = Intent(context, AddMilestoneActivity::class.java)
            intent.putExtra(KEY_PROJECT_ID, projectId)
            if (milestone != null) {
                intent.putExtra(KEY_MILESTONE, milestone)
            }
            return intent
        }
    }

    lateinit var teleprinter: Teleprinter

    var projectId: Long = 0
    var milestone: Milestone? = null
    var currentDate: Date? = null

    private val onDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        currentDate = calendar.time
        bind(calendar.time)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_milestone)
        morph(root)
        teleprinter = Teleprinter(this)
        projectId = intent.getLongExtra(KEY_PROJECT_ID, -1)
        milestone = intent.getParcelableExtra(KEY_MILESTONE)
        if (milestone != null) {
            bind(milestone!!)
            toolbar.inflateMenu(R.menu.edit)
        } else {
            toolbar.inflateMenu(R.menu.create)
        }
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_create, R.id.action_edit -> {
                    createMilestone()
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
        buttonDueDate.setOnClickListener {
            val now = Calendar.getInstance()
            currentDate?.let {
                now.time = it
            }
            val dpd = DatePickerDialog.newInstance(
                    onDateSetListener,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            )
            dpd.accentColor = this.themeAttrColor(R.attr.colorAccent)
            dpd.show(supportFragmentManager, "date_picker")
        }
    }

    private fun createMilestone() {
        teleprinter.hideKeyboard()
        if (!textInputLayoutTitle.checkValid()) {
            return
        }

        progress.visibility = View.VISIBLE
        var dueDate: String? = null
        val currentDate = currentDate
        if (currentDate != null) {
            dueDate = DashDateAdapter.format.format(currentDate)
        }

        if (milestone == null) {
            createOrEditMilestone(App.get().gitLab.createMilestone(projectId,
                    textTitle.text.toString(),
                    textDescription.text.toString(),
                    dueDate))
        } else {
            createOrEditMilestone(App.get().gitLab.editMilestone(projectId,
                    milestone!!.id,
                    textTitle.text.toString(),
                    textDescription.text.toString(),
                    dueDate))
        }

    }

    private fun createOrEditMilestone(observable: Single<Milestone>) {
        observable.with(this)
                .subscribe({
                    progress.visibility = View.GONE
                    if (milestone == null) {
                        App.bus().post(MilestoneCreatedEvent(it))
                    } else {
                        App.bus().post(MilestoneChangedEvent(it))
                    }
                    finish()
                }, {
                    Timber.e(it)
                    progress.visibility = View.GONE
                    showError()
                })
    }

    private fun showError() {
        root.snackbar(R.string.failed_to_create_milestone)
    }

    fun bind(date: Date) {
        buttonDueDate.text = DashDateAdapter.format.format(date)
    }

    fun bind(milestone: Milestone) {
        textTitle.setText(milestone.title)
        if (milestone.description != null) {
            textDescription.setText(milestone.description)
        }
        if (milestone.dueDate != null) {
            currentDate = milestone.dueDate
            bind(currentDate!!)
        }
    }
}
