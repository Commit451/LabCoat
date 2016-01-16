package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.UsersAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.dialog.AccessDialog;
import com.commit451.gitlab.event.MemberAddedEvent;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.util.KeyboardUtil;
import com.commit451.gitlab.util.PaginationUtil;
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
 */
public class AddUserActivity extends MorphActivity {

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

    @Bind(R.id.root) View mRoot;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.search) EditText mUserSearch;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mRecyclerView;
    @Bind(R.id.clear) View mClearView;
    LinearLayoutManager mUserLinearLayoutManager;

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
    UserBasic mSelectedUser;
    long mProjectId;
    Group mGroup;
    String mSearchQuery;
    Uri mNextPageUrl;
    boolean mLoading = false;

    private final View.OnClickListener mOnBackPressed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mUserLinearLayoutManager.getChildCount();
            int totalItemCount = mUserLinearLayoutManager.getItemCount();
            int firstVisibleItem = mUserLinearLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMore();
            }
        }
    };

    private final TextView.OnEditorActionListener mSearchEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (!TextUtils.isEmpty(mUserSearch.getText())) {
                mSearchQuery = mUserSearch.getText().toString();
                loadData();
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

    private final Callback<List<UserBasic>> mUserCallback = new Callback<List<UserBasic>>() {
        @Override
        public void onResponse(Response<List<UserBasic>> response, Retrofit retrofit) {
            mSwipeRefreshLayout.setRefreshing(false);
            mLoading = false;
            if (!response.isSuccess()) {
                return;
            }
            mAdapter.setData(response.body());
            mNextPageUrl = PaginationUtil.parse(response).getNext();
            Timber.d("HAHA Next page url is " + mNextPageUrl);
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mLoading = false;
            Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error_users), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private final Callback<List<UserBasic>> mMoreUsersCallback = new Callback<List<UserBasic>>() {
        @Override
        public void onResponse(Response<List<UserBasic>> response, Retrofit retrofit) {
            mLoading = false;
            if (!response.isSuccess()) {
                return;
            }
            mAdapter.addData(response.body());
            mNextPageUrl = PaginationUtil.parse(response).getNext();
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
        public void onUserClicked(UserBasic user, UserViewHolder userViewHolder) {
            mSelectedUser = user;
            mAccessDialog.show();
        }
    };

    private final AccessDialog.OnAccessAppliedListener mOnAccessAppliedListener = new AccessDialog.OnAccessAppliedListener() {

        @Override
        public void onAccessApplied(int accessLevel) {
            mAccessDialog.showLoading();
            if (mGroup == null) {
                GitLabClient.instance().addProjectMember(
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

    private final Callback<Member> mAddGroupMemeberCallback = new Callback<Member>() {
        @Override
        public void onResponse(Response<Member> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                //Conflict
                if (response.code() == 409) {
                    Snackbar.make(mRoot, R.string.error_user_conflict, Snackbar.LENGTH_SHORT)
                            .show();
                }
                return;
            }
            Snackbar.make(mRoot, R.string.user_added_successfully, Snackbar.LENGTH_SHORT)
                    .show();
            mAccessDialog.dismiss();
            dismiss();
            GitLabApp.bus().post(new MemberAddedEvent(response.body()));
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
        mUserLinearLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mUserLinearLayoutManager);
        mAdapter = new UsersAdapter(mUserClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        morph(mRoot);
    }

    private void loadData() {
        KeyboardUtil.hideKeyboard(AddUserActivity.this);
        mSwipeRefreshLayout.setRefreshing(true);
        mLoading = true;
        GitLabClient.instance().searchUsers(mSearchQuery).enqueue(mUserCallback);
    }

    private void loadMore() {
        mLoading = true;
        Timber.d("loadMore " + mNextPageUrl.toString() + " " + mSearchQuery);
        GitLabClient.instance().searchUsers(mNextPageUrl.toString(), mSearchQuery).enqueue(mMoreUsersCallback);
    }
}
