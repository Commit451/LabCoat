package com.commit451.gitlab.dialog;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.AssigneeAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.UserBasic;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Assign peeps to an issue
 */
public class AssigneeDialog extends AppCompatDialog {

    @Bind(R.id.list) RecyclerView mRecyclerView;
    AssigneeAdapter mAssigneeAdapter;
    @Bind(R.id.progress) View mProgress;
    @Bind(R.id.content_root) View mContentRoot;

    @OnClick(R.id.cancel_button)
    void onCancel() {
        dismiss();
    }

    @OnClick(R.id.assign_button)
    void onAssign() {
        //TODO assign user
        mProgress.setVisibility(View.VISIBLE);
        mContentRoot.animate().alpha(0.0f);
    }

    UserBasic mAssignee;

    private final Callback<List<Member>> mUsersCallback = new Callback<List<Member>>() {

        @Override
        public void onResponse(Response<List<Member>> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                return;
            }
            mAssigneeAdapter.setUsers(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            dismiss();
        }
    };

    public AssigneeDialog(Context context, UserBasic assignee, Project project) {
        super(context);
        setContentView(R.layout.dialog_assignee);
        ButterKnife.bind(this);

        mAssignee = assignee;

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAssigneeAdapter = new AssigneeAdapter(context, assignee);
        mRecyclerView.setAdapter(mAssigneeAdapter);

        GitLabClient.instance().getProjectMembers(project.getId()).enqueue(mUsersCallback);
    }
}
