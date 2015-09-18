package com.commit451.gitlab.dialogs;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.ProjectAccessAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Jawn on 9/16/2015.
 */
public class UserRoleDialog extends AppCompatDialog {

    public interface Listener {
        void onAccessLevelClicked(String accessLevel);
    }
    Listener mListener;

    @Bind(R.id.list) RecyclerView mRecyclerView;
    ProjectAccessAdapter mAdapter;
    String[] mRoleNames;
    String[] mRoleValues;

    private final ProjectAccessAdapter.Listener mAccessListener = new ProjectAccessAdapter.Listener() {
        @Override
        public void onAccessLevelClicked(String accessLevel) {
            for (int i=0; i<mRoleNames.length; i++) {
                if (mRoleNames[i].equals(accessLevel)) {
                    mListener.onAccessLevelClicked(mRoleValues[i]);
                    return;
                }
            }
            Toast.makeText(getContext(), R.string.user_error, Toast.LENGTH_SHORT)
                    .show();
        }
    };

    public UserRoleDialog(Context context, Listener listener) {
        super(context);
        setContentView(R.layout.dialog_user_role);
        ButterKnife.bind(this);
        mRoleValues = getContext().getResources().getStringArray(R.array.role_values);
        mRoleNames = getContext().getResources().getStringArray(R.array.role_names);
        mListener = listener;
        mAdapter = new ProjectAccessAdapter(getContext(), mAccessListener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }
}
