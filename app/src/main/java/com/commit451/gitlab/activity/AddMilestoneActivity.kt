package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.commit451.easel.Easel
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.event.MilestoneChangedEvent
import com.commit451.gitlab.event.MilestoneCreatedEvent
import com.commit451.gitlab.model.api.Milestone
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.teleprinter.Teleprinter
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.parceler.Parcels
import timber.log.Timber
import java.util.*

class AddMilestoneActivity : MorphActivity() {

    companion object {

        private val KEY_PROJECT_ID = "project_id"
        private val KEY_MILESTONE = "milestone"

        @JvmOverloads fun newIntent(context: Context, projectId: Long, milestone: Milestone? = null): Intent {
            val intent = Intent(context, AddMilestoneActivity::class.java)
            intent.putExtra(KEY_PROJECT_ID, projectId)
            if (milestone != null) {
                intent.putExtra(KEY_MILESTONE, Parcels.wrap(milestone))
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

    val onDateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
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
        if (currentDate != null) {
            now.time = currentDate
        }
        val dpd = DatePickerDialog.newInstance(
                onDateSetListener,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        )
        dpd.accentColor = Easel.getThemeAttrColor(this, R.attr.colorAccent)
        dpd.show(fragmentManager, "date_picker")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_milestone)
        ButterKnife.bind(this)
        morph(root)
        teleprinter = Teleprinter(this)
        projectId = intent.getLongExtra(KEY_PROJECT_ID, -1)
        milestone = Parcels.unwrap<Milestone>(intent.getParcelableExtra<Parcelable>(KEY_MILESTONE))
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

    fun createMilestone() {
        teleprinter.hideKeyboard()
        if (TextUtils.isEmpty(textTitle.text)) {
            textInputLayoutTitle.error = getString(R.string.required_field)
            return
        }

        progress.visibility = View.VISIBLE
        var dueDate: String? = null
        if (currentDate != null) {
            dueDate = Milestone.DUE_DATE_FORMAT.format(currentDate)
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

    fun createOrEditMilestone(observable: Single<Milestone>) {
        observable.subscribeOn(Schedulers.io())
                .compose(this.bindToLifecycle<Milestone>())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomSingleObserver<Milestone>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        progress.visibility = View.GONE
                        showError()
                    }

                    override fun success(milestone: Milestone) {
                        progress.visibility = View.GONE
                        if (this@AddMilestoneActivity.milestone == null) {
                            App.bus().post(MilestoneCreatedEvent(milestone))
                        } else {
                            App.bus().post(MilestoneChangedEvent(milestone))
                        }
                        finish()
                    }
                })
    }

    fun showError() {
        Snackbar.make(root, getString(R.string.failed_to_create_milestone), Snackbar.LENGTH_SHORT)
                .show()
    }

    fun bind(date: Date) {
        buttonDueDate.text = Milestone.DUE_DATE_FORMAT.format(date)
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
