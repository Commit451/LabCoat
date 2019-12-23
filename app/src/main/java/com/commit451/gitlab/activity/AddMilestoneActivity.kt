package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
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
import com.google.android.material.textfield.TextInputLayout
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.Single
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

    @BindView(R.id.root)
    lateinit var root: FrameLayout
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.title_text_input_layout)
    lateinit var textInputLayoutTitle: TextInputLayout
    @BindView(R.id.title)
    lateinit var textTitle: EditText
    @BindView(R.id.description)
    lateinit var textDescription: EditText
    @BindView(R.id.due_date)
    lateinit var buttonDueDate: Button
    @BindView(R.id.progress)
    lateinit var progress: View

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
        bind(currentDate!!)
    }

    @OnClick(R.id.due_date)
    fun onDueDateClicked() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_milestone)
        ButterKnife.bind(this)
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
        Snackbar.make(root, getString(R.string.failed_to_create_milestone), Snackbar.LENGTH_SHORT)
                .show()
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
