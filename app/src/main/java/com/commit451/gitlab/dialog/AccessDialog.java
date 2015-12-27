package com.commit451.gitlab.dialog;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.AccessAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Member;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Change a users access level, either for a group or for a project
 * Created by Jawn on 9/16/2015.
 */
public class AccessDialog extends AppCompatDialog {

    @Bind(R.id.list) RecyclerView mRecyclerView;
    AccessAdapter mAdapter;
    @Bind(R.id.progress) View mProgress;
    @Bind(R.id.content_root) View mContentRoot;

    @OnClick(R.id.apply)
    void onApply() {
        String accessLevel = mAdapter.getSelectedValue();
        if (accessLevel == null) {
            Toast.makeText(getContext(), R.string.please_select_access_level, Toast.LENGTH_LONG)
                    .show();
        } else {
            changeAccess(Member.getAccessLevel(accessLevel));
        }
    }

    @OnClick(R.id.cancel_button)
    void onCancel() {
        dismiss();
    }

    OnAccessChangedListener mAccessChangedListener;
    OnAccessAppliedListener mAccessAppliedListener;

    String[] mRoleNames;
    String[] mRoleValues;
    long mProjectId = -1;
    Group mGroup;
    Member mMember;

    private final Callback<Member> mEditUserCallback = new Callback<Member>() {
        @Override
        public void onResponse(Response<Member> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                onError();
                return;
            }
            if (mAccessChangedListener != null) {
                mAccessChangedListener.onAccessChanged(mMember, mAdapter.getSelectedValue());
            }
            dismiss();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(null, t);
            onError();
        }
    };

    public AccessDialog(Context context, OnAccessAppliedListener accessAppliedListener) {
        this(context, null, null, -1);
        mAccessAppliedListener = accessAppliedListener;
    }

    public AccessDialog(Context context, Member member, Group group) {
        this(context, member, group, -1);
    }

    public AccessDialog(Context context, Member member, long projectId) {
        this(context, member, null, projectId);
    }

    private AccessDialog(Context context, Member member, Group group, long projectId) {
        super(context);
        setContentView(R.layout.dialog_access);
        ButterKnife.bind(this);
        mMember = member;
        if (group == null) {
            mRoleValues = getContext().getResources().getStringArray(R.array.project_role_values);
            mRoleNames = getContext().getResources().getStringArray(R.array.project_role_names);
        } else {
            mRoleValues = getContext().getResources().getStringArray(R.array.group_role_values);
            mRoleNames = getContext().getResources().getStringArray(R.array.group_role_names);
        }
        mGroup = group;
        mProjectId = projectId;
        mAdapter = new AccessAdapter(getContext(), mRoleNames);
        if (mMember != null) {
            mAdapter.setSelectedAccess(Member.getAccessLevel(mMember.getAccessLevel()));
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void changeAccess(int accessLevel) {

        if (mGroup != null) {
            showLoading();
            GitLabClient.instance().editGroupMember(mGroup.getId(), mMember.getId(), accessLevel).enqueue(mEditUserCallback);
        } else if (mProjectId != -1) {
            showLoading();
            GitLabClient.instance().editProjectMember(mProjectId, mMember.getId(), accessLevel).enqueue(mEditUserCallback);
        } else if (mAccessAppliedListener != null) {
            mAccessAppliedListener.onAccessApplied(accessLevel);
        } else {
            throw new IllegalStateException("Not sure what to apply this access change to. Check the constructors plz");
        }
    }

    private void showLoading() {
        mContentRoot.animate().alpha(0.0f);
        mProgress.setVisibility(View.VISIBLE);
    }

    private void onError() {
        Toast.makeText(getContext(), R.string.failed_to_apply_access_level, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    public void setOnAccessChangedListener(OnAccessChangedListener listener) {
        mAccessChangedListener = listener;
    }

    public interface OnAccessChangedListener {
        void onAccessChanged(Member member, String accessLevel);
    }

    public interface OnAccessAppliedListener {
        void onAccessApplied(int accessLevel);
    }
}
