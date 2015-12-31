package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.model.api.Diff;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.view.DiffView;
import com.commit451.gitlab.view.MessageView;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class DiffActivity extends BaseActivity {

    private static final String EXTRA_PROJECT = "extra_project";
    private static final String EXTRA_COMMIT = "extra_commit";

    public static Intent newInstance(Context context, Project project, RepositoryCommit commit) {
        Intent intent = new Intent(context, DiffActivity.class);
        intent.putExtra(EXTRA_PROJECT, Parcels.wrap(project));
        intent.putExtra(EXTRA_COMMIT, Parcels.wrap(commit));
        return intent;
    }

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.message_container) LinearLayout mMessageContainer;
    @Bind(R.id.diff_container) LinearLayout mDiffContainer;

    private Project mProject;
    private RepositoryCommit mCommit;
    private boolean textWrapped = true;

    private Callback<RepositoryCommit> mCommitCallback = new Callback<RepositoryCommit>() {
        @Override
        public void onResponse(Response<RepositoryCommit> response, Retrofit retrofit) {
            if (response.isSuccess()) {
                mMessageContainer.removeAllViews();

                MessageView messageView = new MessageView(DiffActivity.this, response.body());
                messageView.setWrapped(textWrapped);
                mMessageContainer.addView(messageView);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private Callback<List<Diff>> mDiffCallback = new Callback<List<Diff>>() {
        @Override
        public void onResponse(Response<List<Diff>> response, Retrofit retrofit) {
            if (response.isSuccess()) {
                mDiffContainer.removeAllViews();

                for (Diff diff : response.body()) {
                    DiffView diffView = new DiffView(DiffActivity.this, diff);
                    diffView.setWrapped(textWrapped);
                    mDiffContainer.addView(diffView);
                }
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff);
        ButterKnife.bind(this);

        mProject = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));
        mCommit = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_COMMIT));

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setTitle(mCommit.getShortId());
        mToolbar.inflateMenu(R.menu.menu_diff);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case android.R.id.home:
                        finish();
                        return true;
                    case R.id.text_wrap_checkbox:
                        textWrapped = !item.isChecked();
                        item.setChecked(textWrapped);
                        updateTextWrap(textWrapped);
                        return true;
                }
                return false;
            }
        });

        //TODO make this use RecyclerViews, cause this is insane
        GitLabClient.instance().getCommit(mProject.getId(), mCommit.getId()).enqueue(mCommitCallback);
        GitLabClient.instance().getCommitDiff(mProject.getId(), mCommit.getId()).enqueue(mDiffCallback);
    }

    private void updateTextWrap(boolean checked) {
        for (int i = 0; i < mMessageContainer.getChildCount(); i++) {
            ((MessageView) mMessageContainer.getChildAt(i)).setWrapped(checked);
        }

        for (int i = 0; i < mDiffContainer.getChildCount(); i++) {
            ((DiffView) mDiffContainer.getChildAt(i)).setWrapped(checked);
        }
    }
}