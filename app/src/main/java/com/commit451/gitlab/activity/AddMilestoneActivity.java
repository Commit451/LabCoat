package com.commit451.gitlab.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.MilestoneChangedEvent;
import com.commit451.gitlab.event.MilestoneCreatedEvent;
import com.commit451.gitlab.model.api.Milestone;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.parceler.Parcels;

import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class AddMilestoneActivity extends BaseActivity {

    private static final String KEY_PROJECT_ID = "project_id";
    private static final String KEY_MILESTONE = "milestone";

    public static Intent newInstance(Context context, long projectId) {
        return newInstance(context, projectId, null);
    }

    public static Intent newInstance(Context context, long projectId, Milestone milestone) {
        Intent intent = new Intent(context, AddMilestoneActivity.class);
        intent.putExtra(KEY_PROJECT_ID, projectId);
        if (milestone != null) {
            intent.putExtra(KEY_MILESTONE, Parcels.wrap(milestone));
        }
        return intent;
    }

    @Bind(R.id.root)
    View mRoot;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.title_text_input_layout)
    TextInputLayout mTitleTextInputLayout;
    @Bind(R.id.title)
    EditText mTitle;
    @Bind(R.id.description)
    EditText mDescription;
    @Bind(R.id.due_date)
    Button mDueDate;
    @Bind(R.id.progress)
    View mProgress;

    @OnClick(R.id.due_date)
    void onDueDateClicked() {
        Calendar now = Calendar.getInstance();
        if (mCurrentDate != null) {
            now.setTime(mCurrentDate);
        }
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                mOnDateSetListener,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getFragmentManager(), "date_picker");
    }

    long mProjectId;
    Milestone mMilestone;
    Date mCurrentDate;

    private final DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            mCurrentDate = calendar.getTime();
            bind(mCurrentDate);
        }
    };

    private final View.OnClickListener mOnBackPressed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    private Callback<Milestone> mMilestoneCallback = new Callback<Milestone>() {

        @Override
        public void onResponse(Response<Milestone> response, Retrofit retrofit) {
            mProgress.setVisibility(View.GONE);
            if (!response.isSuccess()) {
                showError();
                return;
            }
            if (mMilestone == null) {
                GitLabApp.bus().post(new MilestoneCreatedEvent(response.body()));
            } else {
                GitLabApp.bus().post(new MilestoneChangedEvent(response.body()));
            }
            finish();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            showError();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_milestone);
        ButterKnife.bind(this);
        mProjectId = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        mMilestone = Parcels.unwrap(getIntent().getParcelableExtra(KEY_MILESTONE));
        if (mMilestone != null) {
            bind(mMilestone);
            mToolbar.inflateMenu(R.menu.menu_edit_milestone);
        } else {
            mToolbar.inflateMenu(R.menu.menu_add_milestone);
        }
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(mOnBackPressed);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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
        if (TextUtils.isEmpty(mTitle.getText())) {
            mTitleTextInputLayout.setError(getString(R.string.required_field));
            return;
        }

        mProgress.setVisibility(View.VISIBLE);
        String dueDate = null;
        if (mCurrentDate != null) {
            dueDate = Milestone.DUE_DATE_FORMAT.format(mCurrentDate);
        }

        if (mMilestone == null) {
            GitLabClient.instance().createMilestone(mProjectId,
                    mTitle.getText().toString(),
                    mDescription.getText().toString(),
                    dueDate).enqueue(mMilestoneCallback);
        } else {
            GitLabClient.instance().editMilestone(mProjectId,
                    mMilestone.getId(),
                    mTitle.getText().toString(),
                    mDescription.getText().toString(),
                    dueDate).enqueue(mMilestoneCallback);
        }

    }

    private void showError() {
        Snackbar.make(mRoot, getString(R.string.failed_to_create_milestone), Snackbar.LENGTH_SHORT)
                .show();
    }

    private void bind(Date date) {
        mDueDate.setText(Milestone.DUE_DATE_FORMAT.format(date));
    }

    private void bind(Milestone milestone) {
        mTitle.setText(milestone.getTitle());
        if (milestone.getDescription() != null) {
            mDescription.setText(milestone.getDescription());
        }
        if (milestone.getDueDate() != null) {
            mCurrentDate = milestone.getDueDate();
            bind(mCurrentDate);
        }
    }
}
