package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.UsersAdapter;
import com.commit451.gitlab.animation.HideRunnable;
import com.commit451.gitlab.dialog.AccessDialog;
import com.commit451.gitlab.event.MemberAddedEvent;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.gitlab.viewHolder.UserViewHolder;
import com.commit451.reptar.FocusedSingleObserver;
import com.commit451.reptar.retrofit.ResponseSingleObserver;
import com.commit451.teleprinter.Teleprinter;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Add a new user to the repo or to the group, depending on the mode
 */
public class AddUserActivity extends MorphActivity {

    private static final String KEY_PROJECT_ID = "project_id";
    private static final String KEY_GROUP = "group";

    public static Intent newIntent(Context context, long projectId) {
        Intent intent = new Intent(context, AddUserActivity.class);
        intent.putExtra(KEY_PROJECT_ID, projectId);
        return intent;
    }

    public static Intent newIntent(Context context, Group group) {
        Intent intent = new Intent(context, AddUserActivity.class);
        intent.putExtra(KEY_GROUP, Parcels.wrap(group));
        return intent;
    }

    @BindView(R.id.root)
    ViewGroup mRoot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.search)
    EditText mUserSearch;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView mRecyclerView;
    @BindView(R.id.clear)
    View mClearView;
    GridLayoutManager mUserLinearLayoutManager;

    @OnClick(R.id.clear)
    void onClearClick() {
        mClearView.animate().alpha(0.0f).withEndAction(new Runnable() {
            @Override
            public void run() {
                mClearView.setVisibility(View.GONE);
                mUserSearch.getText().clear();
                mTeleprinter.showKeyboard(mUserSearch);
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
    Teleprinter mTeleprinter;


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
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(s)) {
                mClearView.animate()
                        .alpha(0.0f)
                        .withEndAction(new HideRunnable(mClearView));
            } else {
                mClearView.setVisibility(View.VISIBLE);
                mClearView.animate().alpha(1.0f);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
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
                add(App.get().getGitLab().addProjectMember(mProjectId, mSelectedUser.getId(), accessLevel));
            } else {
                add(App.get().getGitLab().addGroupMember(mProjectId, mSelectedUser.getId(), accessLevel));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        ButterKnife.bind(this);
        mTeleprinter = new Teleprinter(this);
        mProjectId = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        mGroup = Parcels.unwrap(getIntent().getParcelableExtra(KEY_GROUP));
        mAccessDialog = new AccessDialog(this, mOnAccessAppliedListener);
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(mOnBackPressed);
        mUserSearch.setOnEditorActionListener(mSearchEditorActionListener);
        mUserSearch.addTextChangedListener(mTextWatcher);
        mAdapter = new UsersAdapter(mUserClickListener);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mUserLinearLayoutManager = new GridLayoutManager(this, 2);
        mUserLinearLayoutManager.setSpanSizeLookup(mAdapter.getSpanSizeLookup());
        mRecyclerView.setLayoutManager(mUserLinearLayoutManager);
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        morph(mRoot);
    }

    private void loadData() {
        mTeleprinter.hideKeyboard();
        mSwipeRefreshLayout.setRefreshing(true);
        mLoading = true;
        App.get().getGitLab().searchUsers(mSearchQuery)
                .compose(this.<Response<List<UserBasic>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseSingleObserver<List<UserBasic>>() {

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mSwipeRefreshLayout.setRefreshing(false);
                        mLoading = false;
                        Snackbar.make(mRoot, getString(R.string.connection_error_users), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    protected void onResponseSuccess(List<UserBasic> users) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mLoading = false;
                        mAdapter.setData(users);
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        Timber.d("Next page url is %s", mNextPageUrl);
                    }
                });
    }

    private void loadMore() {
        mLoading = true;
        mAdapter.setLoading(true);
        Timber.d("loadMore " + mNextPageUrl.toString() + " " + mSearchQuery);
        App.get().getGitLab().searchUsers(mNextPageUrl.toString(), mSearchQuery)
                .compose(this.<Response<List<UserBasic>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseSingleObserver<List<UserBasic>>() {

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mAdapter.setLoading(false);
                    }

                    @Override
                    protected void onResponseSuccess(List<UserBasic> users) {
                        mLoading = false;
                        mAdapter.setLoading(false);
                        mAdapter.addData(users);
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                    }
                });
    }

    private void add(Single<Response<Member>> observable) {
        observable.subscribeOn(Schedulers.io())
                .compose(this.<Response<Member>>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FocusedSingleObserver<Response<Member>>() {

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        Snackbar.make(mRoot, R.string.error_failed_to_add_user, Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(Response<Member> response) {
                        if (response.isSuccessful()) {
                            Snackbar.make(mRoot, R.string.user_added_successfully, Snackbar.LENGTH_SHORT)
                                    .show();
                            mAccessDialog.dismiss();
                            dismiss();
                            App.bus().post(new MemberAddedEvent(response.body()));
                        } else {
                            if (response.code() == 409) {
                                Snackbar.make(mRoot, R.string.error_user_conflict, Snackbar.LENGTH_SHORT)
                                        .show();
                            } else {
                                onError(new Exception("Does not matter"));
                            }
                        }
                    }
                });
    }
}
