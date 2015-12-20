package com.commit451.gitlab.activities;

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
import com.commit451.gitlab.model.Commit;
import com.commit451.gitlab.model.Diff;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.views.DiffView;
import com.commit451.gitlab.views.MessageView;

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

    public static Intent newInstance(Context context, Project project, Commit commit) {
        Intent intent = new Intent(context, DiffActivity.class);
        intent.putExtra(EXTRA_PROJECT, Parcels.wrap(project));
        intent.putExtra(EXTRA_COMMIT, Parcels.wrap(commit));
        return intent;
    }

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.message_container) LinearLayout messageContainer;
    @Bind(R.id.diff_container) LinearLayout diffContainer;

    Project mProject;
    Commit mCommit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff);
        ButterKnife.bind(this);
        mProject = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));
        mCommit = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_COMMIT));
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(mCommit.getShortId());
        toolbar.inflateMenu(R.menu.menu_diff);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case android.R.id.home:
                        finish();
                        return true;
                    case R.id.text_wrap_checkbox:
                        item.setChecked(!item.isChecked());
                        setTextWrap(item.isChecked());
                        return true;
                }
                return false;
            }
        });

        //TODO make this use RecyclerViews, cause this is insane
        GitLabClient.instance().getCommit(mProject.getId(), mCommit.getId()).enqueue(commitCallback);
        GitLabClient.instance().getCommitDiff(mProject.getId(), mCommit.getId()).enqueue(diffCallback);
    }

    private Callback<Commit> commitCallback = new Callback<Commit>() {

        @Override
        public void onResponse(Response<Commit> response, Retrofit retrofit) {
            if (response.isSuccess()) {
                messageContainer.addView(new MessageView(DiffActivity.this, response.body()));
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private Callback<List<Diff>> diffCallback = new Callback<List<Diff>>() {

        @Override
        public void onResponse(Response<List<Diff>> response, Retrofit retrofit) {
            if (response.isSuccess()) {
                for(Diff diff : response.body()) {
                    diffContainer.addView(new DiffView(DiffActivity.this, diff));
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

    private void setTextWrap(boolean checked) {
        ((MessageView) messageContainer.getChildAt(0)).setWrapped(checked);

        for(int i = 0; i < diffContainer.getChildCount(); ++i) {
            ((DiffView) diffContainer.getChildAt(i)).setWrapped(checked);
        }
    }
}