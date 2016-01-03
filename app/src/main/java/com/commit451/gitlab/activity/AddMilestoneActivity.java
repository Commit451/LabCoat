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
import com.commit451.gitlab.event.MilestoneCreatedEvent;
import com.commit451.gitlab.model.api.Milestone;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class AddMilestoneActivity extends BaseActivity {

    private static final String KEY_PROJECT_ID = "project_id";

    public static Intent newInstance(Context context, long projectId) {
        Intent intent = new Intent(context, AddMilestoneActivity.class);
        intent.putExtra(KEY_PROJECT_ID, projectId);
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
        //TODO bring up the calendar dialog
    }

    long mProjectId;

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
            GitLabApp.bus().post(new MilestoneCreatedEvent(response.body()));
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
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(mOnBackPressed);
        mToolbar.inflateMenu(R.menu.menu_add_milestone);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_create:
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
        //TODO make this the current due date
        GitLabClient.instance().createMilestone(mProjectId,
                mTitle.getText().toString(),
                mDescription.getText().toString(),
                dueDate).enqueue(mMilestoneCallback);

    }

    private void showError() {
        Snackbar.make(mRoot, getString(R.string.failed_to_create_milestone), Snackbar.LENGTH_SHORT)
                .show();
    }
}
