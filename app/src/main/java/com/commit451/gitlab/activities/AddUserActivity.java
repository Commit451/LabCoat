package com.commit451.gitlab.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.MemberAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.dialogs.UserRoleDialog;
import com.commit451.gitlab.events.UserAddedEvent;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.KeyboardUtil;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

/**
 * Add a new user to the repo
 * Created by Jawn on 9/15/2015.
 */
public class AddUserActivity extends BaseActivity {

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, AddUserActivity.class);
        return intent;
    }

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.userSearch) EditText mUserSearch;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mRecyclerView;
    MemberAdapter mAdapter;
    UserRoleDialog mUserRoleDialog;
    User mSelectedUser;
    private final View.OnClickListener mOnBackPressed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    private final TextView.OnEditorActionListener mSearchEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (!TextUtils.isEmpty(mUserSearch.getText())) {
                KeyboardUtil.hideKeyboard(AddUserActivity.this);
                mSwipeRefreshLayout.setRefreshing(true);
                GitLabClient.instance().searchUsers(mUserSearch.getText().toString()).enqueue(mUserCallback);
            }
            return true;
        }
    };

    private final Callback<List<User>> mUserCallback = new Callback<List<User>>() {
        @Override
        public void onResponse(Response<List<User>> response) {
            mSwipeRefreshLayout.setRefreshing(false);
            if (!response.isSuccess()) {
                return;
            }
            mAdapter.setData(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t.toString());
            Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error_users), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private final MemberAdapter.Listener mUserClickListener = new MemberAdapter.Listener() {
        @Override
        public void onUserClicked(User user) {
            mSelectedUser = user;
            mUserRoleDialog.show();
        }
    };

    private final UserRoleDialog.Listener mUserRoleDialogListener = new UserRoleDialog.Listener() {
        @Override
        public void onAccessLevelClicked(String accessLevel) {
            //TODO fix this cause yeah...
//            GitLabClient.instance().addGroupMember(
//                    GitLabApp.instance().getSelectedProject().getGroup().getId(),
//                    mSelectedUser.getId(),
//                    accessLevel).enqueue(mAddGroupMemeberCallback);
        }
    };

    private final Callback<User> mAddGroupMemeberCallback = new Callback<User>() {
        @Override
        public void onResponse(Response<User> response) {
            if (!response.isSuccess()) {
                //Conflict
                if (response.code() == 409) {
                    Toast.makeText(AddUserActivity.this, R.string.error_user_conflict, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            Toast.makeText(AddUserActivity.this, R.string.user_added_successfully, Toast.LENGTH_SHORT).show();
            mUserRoleDialog.dismiss();
            finish();
            GitLabApp.bus().post(new UserAddedEvent(response.body()));
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        ButterKnife.bind(this);
        mUserRoleDialog = new UserRoleDialog(this, mUserRoleDialogListener);
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(mOnBackPressed);
        mUserSearch.setOnEditorActionListener(mSearchEditorActionListener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MemberAdapter(mUserClickListener);
        mRecyclerView.setAdapter(mAdapter);
    }
}
