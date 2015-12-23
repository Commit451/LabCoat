package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.UsersAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.dialog.AccessDialog;
import com.commit451.gitlab.event.UserAddedEvent;
import com.commit451.gitlab.model.Group;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.util.KeyboardUtil;
import com.commit451.gitlab.viewHolder.UserViewHolder;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Add a new user to the repo or to the group, depending on the mode
 * Created by Jawn on 9/15/2015.
 */
public class AddUserActivity extends BaseActivity {

    private static final String KEY_PROJECT_ID = "project_id";
    private static final String KEY_GROUP = "group";

    public static Intent newInstance(Context context, long projectId) {
        Intent intent = new Intent(context, AddUserActivity.class);
        intent.putExtra(KEY_PROJECT_ID, projectId);
        return intent;
    }

    public static Intent newIntent(Context context, Group group) {
        Intent intent = new Intent(context, AddUserActivity.class);
        intent.putExtra(KEY_GROUP, Parcels.wrap(group));
        return intent;
    }

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.search) EditText mUserSearch;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mRecyclerView;
    @Bind(R.id.clear) View mClearView;

    @OnClick(R.id.clear)
    void onClearClick() {
        mClearView.animate().alpha(0.0f).withEndAction(new Runnable() {
            @Override
            public void run() {
                mClearView.setVisibility(View.GONE);
                mUserSearch.getText().clear();
            }
        });
    }

    UsersAdapter mAdapter;
    AccessDialog mAccessDialog;
    User mSelectedUser;
    long mProjectId;
    Group mGroup;

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

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(s)) {
                mClearView.animate().alpha(0.0f).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mClearView.setVisibility(View.GONE);
                    }
                });
            } else {
                mClearView.setVisibility(View.VISIBLE);
                mClearView.animate().alpha(1.0f);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private final Callback<List<User>> mUserCallback = new Callback<List<User>>() {
        @Override
        public void onResponse(Response<List<User>> response, Retrofit retrofit) {
            mSwipeRefreshLayout.setRefreshing(false);
            if (!response.isSuccess()) {
                return;
            }
            mAdapter.setData(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error_users), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private final UsersAdapter.Listener mUserClickListener = new UsersAdapter.Listener() {
        @Override
        public void onUserClicked(User user, UserViewHolder userViewHolder) {
            mSelectedUser = user;
            mAccessDialog.show();
        }
    };

    private final AccessDialog.OnAccessAppliedListener mOnAccessAppliedListener = new AccessDialog.OnAccessAppliedListener() {

        @Override
        public void onAccessApplied(String accessLevel) {
            if (mGroup == null) {
                GitLabClient.instance().addProjectTeamMember(
                        mProjectId,
                        mSelectedUser.getId(),
                        accessLevel).enqueue(mAddGroupMemeberCallback);
            } else {
                GitLabClient.instance().addGroupMember(mGroup.getId(),
                        mSelectedUser.getId(),
                        accessLevel).enqueue(mAddGroupMemeberCallback);
            }
        }
    };

    private final Callback<User> mAddGroupMemeberCallback = new Callback<User>() {
        @Override
        public void onResponse(Response<User> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                //Conflict
                if (response.code() == 409) {
                    Toast.makeText(AddUserActivity.this, R.string.error_user_conflict, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            Toast.makeText(AddUserActivity.this, R.string.user_added_successfully, Toast.LENGTH_SHORT).show();
            mAccessDialog.dismiss();
            finish();
            GitLabApp.bus().post(new UserAddedEvent(response.body()));
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        ButterKnife.bind(this);
        mProjectId = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        mGroup = Parcels.unwrap(getIntent().getParcelableExtra(KEY_GROUP));
        mAccessDialog = new AccessDialog(this, mOnAccessAppliedListener);
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(mOnBackPressed);
        mUserSearch.setOnEditorActionListener(mSearchEditorActionListener);
        mUserSearch.addTextChangedListener(mTextWatcher);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mAdapter = new UsersAdapter(mUserClickListener);
        mRecyclerView.setAdapter(mAdapter);
    }
}
