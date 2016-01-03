package com.commit451.gitlab.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.ArcMotion;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.Toast;

import com.commit451.easel.Easel;
import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.IssueChangedEvent;
import com.commit451.gitlab.event.IssueCreatedEvent;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.transition.MorphDialogToFab;
import com.commit451.gitlab.transition.MorphFabToDialog;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Dialog to input new issues, but not really a dialog at all wink wink
 */
public class AddIssueActivity extends BaseActivity {

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

    @Bind(R.id.root) ViewGroup mContainer;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.title_text_input_layout) TextInputLayout mTitleInputLayout;
    @Bind(R.id.title) EditText mTitleInput;
    @Bind(R.id.description) EditText mDescriptionInput;
    @Bind(R.id.progress) View mProgress;

    private Project mProject;
    private Issue mIssue;

    private void save() {
        if(!TextUtils.isEmpty(mTitleInput.getText())) {
            mTitleInputLayout.setError(null);
            showLoading();
            if (mIssue == null) {
                GitLabClient.instance().createIssue(mProject.getId(), mTitleInput.getText().toString().trim(), mDescriptionInput.getText().toString().trim())
                        .enqueue(mIssueCallback);
            } else {
                GitLabClient.instance().updateIssue(mProject.getId(), mIssue.getId(), mTitleInput.getText().toString(), mDescriptionInput.getText().toString())
                        .enqueue(mIssueCallback);
            }
        }
        else {
            mTitleInputLayout.setError(getString(R.string.required_field));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_issue);
        ButterKnife.bind(this);

        mProject = Parcels.unwrap(getIntent().getParcelableExtra(KEY_PROJECT));
        mIssue = Parcels.unwrap(getIntent().getParcelableExtra(KEY_ISSUE));

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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

        if (mIssue != null) {
            bindIssue();
            mToolbar.inflateMenu(R.menu.menu_edit_milestone);
        } else {
            mToolbar.inflateMenu(R.menu.menu_add_milestone);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            int fabColor = Easel.getThemeAttrColor(this, R.attr.colorAccent);
            int dialogColor = ContextCompat.getColor(this, R.color.grey);
            setupSharedElementTransitionsFab(this, mContainer,
                    fabColor,
                    dialogColor,
                    getResources().getDimensionPixelSize(R.dimen.dialog_corners));
        }
    }

    @TargetApi(21)
    public void setupSharedElementTransitionsFab(@NonNull Activity activity,
                                                 @Nullable View target,
                                                 int fabColor,
                                                 int dialogColor,
                                                 int dialogCornerRadius) {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(50f);
        arcMotion.setMinimumVerticalAngle(50f);
        Interpolator easeInOut = AnimationUtils.loadInterpolator(activity, android.R.interpolator.fast_out_slow_in);
        MorphFabToDialog sharedEnter = new MorphFabToDialog(fabColor, dialogColor, dialogCornerRadius);
        sharedEnter.setPathMotion(arcMotion);
        sharedEnter.setInterpolator(easeInOut);
        MorphDialogToFab sharedReturn = new MorphDialogToFab(dialogColor, fabColor);
        sharedReturn.setPathMotion(arcMotion);
        sharedReturn.setInterpolator(easeInOut);
        if (target != null) {
            sharedEnter.addTarget(target);
            sharedReturn.addTarget(target);
        }
        activity.getWindow().setSharedElementEnterTransition(sharedEnter);
        activity.getWindow().setSharedElementReturnTransition(sharedReturn);
    }

    @TargetApi(21)
    public void dismiss() {
        if (Build.VERSION.SDK_INT >= 21) {
            finishAfterTransition();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    private Callback<Issue> mIssueCallback = new Callback<Issue>() {

        @Override
        public void onResponse(Response<Issue> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                Toast.makeText(AddIssueActivity.this, getString(R.string.failed_to_create_issue), Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            if (mIssue == null) {
                GitLabApp.bus().post(new IssueCreatedEvent(response.body()));
            } else {
                GitLabApp.bus().post(new IssueChangedEvent(response.body()));
            }
            dismiss();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            Toast.makeText(AddIssueActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT)
                    .show();
        }
    };

    private void showLoading() {
        mProgress.setVisibility(View.VISIBLE);
        mProgress.setAlpha(0.0f);
        mProgress.animate().alpha(1.0f);
    }

    private void bindIssue() {
        if (!TextUtils.isEmpty(mIssue.getTitle())) {
            mTitleInput.setText(mIssue.getTitle());
        }
        if (!TextUtils.isEmpty(mIssue.getDescription())) {
            mDescriptionInput.setText(mIssue.getDescription());
        }
    }

}