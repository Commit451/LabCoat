package com.commit451.gitlab.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.commit451.easel.Easel;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.event.MilestoneChangedEvent;
import com.commit451.gitlab.event.MilestoneCreatedEvent;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.teleprinter.Teleprinter;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.parceler.Parcels;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class AddMilestoneActivity extends MorphActivity {

    private static final String KEY_PROJECT_ID = "project_id";
    private static final String KEY_MILESTONE = "milestone";

    public static Intent newIntent(Context context, long projectId) {
        return newIntent(context, projectId, null);
    }

    public static Intent newIntent(Context context, long projectId, Milestone milestone) {
        Intent intent = new Intent(context, AddMilestoneActivity.class);
        intent.putExtra(KEY_PROJECT_ID, projectId);
        if (milestone != null) {
            intent.putExtra(KEY_MILESTONE, Parcels.wrap(milestone));
        }
        return intent;
    }

    @BindView(R.id.root)
    FrameLayout root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title_text_input_layout)
    TextInputLayout textInputLayoutTitle;
    @BindView(R.id.title)
    EditText textTitle;
    @BindView(R.id.description)
    EditText textDescription;
    @BindView(R.id.due_date)
    Button buttonDueDate;
    @BindView(R.id.progress)
    View progress;

    Teleprinter teleprinter;

    long projectId;
    Milestone milestone;
    Date currentDate;

    private final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            currentDate = calendar.getTime();
            bind(currentDate);
        }
    };

    @OnClick(R.id.due_date)
    void onDueDateClicked() {
        Calendar now = Calendar.getInstance();
        if (currentDate != null) {
            now.setTime(currentDate);
        }
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                onDateSetListener,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setAccentColor(Easel.getThemeAttrColor(this, R.attr.colorAccent));
        dpd.show(getFragmentManager(), "date_picker");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_milestone);
        ButterKnife.bind(this);
        morph(root);
        teleprinter = new Teleprinter(this);
        projectId = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        milestone = Parcels.unwrap(getIntent().getParcelableExtra(KEY_MILESTONE));
        if (milestone != null) {
            bind(milestone);
            toolbar.inflateMenu(R.menu.menu_edit_milestone);
        } else {
            toolbar.inflateMenu(R.menu.menu_add_milestone);
        }
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
                        createMilestone();
                        return true;
                }
                return false;
            }
        });
    }

    private void createMilestone() {
        teleprinter.hideKeyboard();
        if (TextUtils.isEmpty(textTitle.getText())) {
            textInputLayoutTitle.setError(getString(R.string.required_field));
            return;
        }

        progress.setVisibility(View.VISIBLE);
        String dueDate = null;
        if (currentDate != null) {
            dueDate = Milestone.DUE_DATE_FORMAT.format(currentDate);
        }

        if (milestone == null) {
            createOrEditMilestone(App.get().getGitLab().createMilestone(projectId,
                    textTitle.getText().toString(),
                    textDescription.getText().toString(),
                    dueDate));
        } else {
            createOrEditMilestone(App.get().getGitLab().editMilestone(projectId,
                    milestone.getId(),
                    textTitle.getText().toString(),
                    textDescription.getText().toString(),
                    dueDate));
        }

    }

    private void createOrEditMilestone(Single<Milestone> observable) {
        observable.subscribeOn(Schedulers.io())
                .compose(this.<Milestone>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Milestone>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        progress.setVisibility(View.GONE);
                        showError();
                    }

                    @Override
                    public void success(@NonNull Milestone milestone) {
                        progress.setVisibility(View.GONE);
                        if (AddMilestoneActivity.this.milestone == null) {
                            App.bus().post(new MilestoneCreatedEvent(milestone));
                        } else {
                            App.bus().post(new MilestoneChangedEvent(milestone));
                        }
                        finish();
                    }
                });
    }

    private void showError() {
        Snackbar.make(root, getString(R.string.failed_to_create_milestone), Snackbar.LENGTH_SHORT)
                .show();
    }

    private void bind(Date date) {
        buttonDueDate.setText(Milestone.DUE_DATE_FORMAT.format(date));
    }

    private void bind(Milestone milestone) {
        textTitle.setText(milestone.getTitle());
        if (milestone.getDescription() != null) {
            textDescription.setText(milestone.getDescription());
        }
        if (milestone.getDueDate() != null) {
            currentDate = milestone.getDueDate();
            bind(currentDate);
        }
    }
}
