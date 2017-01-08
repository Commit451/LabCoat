package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.commit451.alakazam.HideRunnable;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.UserAdapter;
import com.commit451.gitlab.dialog.AccessDialog;
import com.commit451.gitlab.event.MemberAddedEvent;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.gitlab.viewHolder.UserViewHolder;
import com.commit451.teleprinter.Teleprinter;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
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
    ViewGroup root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search)
    EditText textSearch;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView list;
    @BindView(R.id.clear)
    View buttonClear;

    GridLayoutManager layoutManager;
    UserAdapter adapter;
    AccessDialog dialogAccess;
    UserBasic selectedUser;
    Teleprinter teleprinter;

    long projectId;
    Group group;
    String query;
    Uri nextPageUrl;
    boolean loading = false;

    @OnClick(R.id.clear)
    void onClearClick() {
        buttonClear.animate().alpha(0.0f).withEndAction(new Runnable() {
            @Override
            public void run() {
                buttonClear.setVisibility(View.GONE);
                textSearch.getText().clear();
                teleprinter.showKeyboard(textSearch);
            }
        });
    }

    @OnEditorAction(R.id.search)
    boolean onEditorAction() {
        if (!TextUtils.isEmpty(textSearch.getText())) {
            query = textSearch.getText().toString();
            loadData();
        }
        return true;
    }

    @OnTextChanged(R.id.search)
    void onTextChanged(CharSequence s, int start, int before, int count) {
        if (TextUtils.isEmpty(s)) {
            buttonClear.animate()
                    .alpha(0.0f)
                    .withEndAction(new HideRunnable(buttonClear));
        } else {
            buttonClear.setVisibility(View.VISIBLE);
            buttonClear.animate().alpha(1.0f);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        ButterKnife.bind(this);
        teleprinter = new Teleprinter(this);
        projectId = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        group = Parcels.unwrap(getIntent().getParcelableExtra(KEY_GROUP));
        dialogAccess = new AccessDialog(this, new AccessDialog.Listener() {
            @Override
            public void onAccessApplied(int accessLevel) {
                dialogAccess.showLoading();
                if (group == null) {
                    add(App.get().getGitLab().addProjectMember(projectId, selectedUser.getId(), accessLevel));
                } else {
                    add(App.get().getGitLab().addGroupMember(projectId, selectedUser.getId(), accessLevel));
                }
            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        adapter = new UserAdapter(new UserAdapter.Listener() {
            @Override
            public void onUserClicked(UserBasic user, UserViewHolder userViewHolder) {
                selectedUser = user;
                dialogAccess.show();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        list.setAdapter(adapter);
        layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(adapter.getSpanSizeLookup());
        list.setLayoutManager(layoutManager);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                    loadMore();
                }
            }
        });

        morph(root);
    }

    private void loadData() {
        teleprinter.hideKeyboard();
        swipeRefreshLayout.setRefreshing(true);
        loading = true;
        App.get().getGitLab().searchUsers(query)
                .compose(this.<Response<List<UserBasic>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<UserBasic>>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        swipeRefreshLayout.setRefreshing(false);
                        loading = false;
                        Snackbar.make(root, getString(R.string.connection_error_users), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void responseSuccess(@NonNull List<UserBasic> users) {
                        swipeRefreshLayout.setRefreshing(false);
                        loading = false;
                        adapter.setData(users);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        Timber.d("Next page url is %s", nextPageUrl);
                    }
                });
    }

    private void loadMore() {
        loading = true;
        adapter.setLoading(true);
        Timber.d("loadMore " + nextPageUrl.toString() + " " + query);
        App.get().getGitLab().searchUsers(nextPageUrl.toString(), query)
                .compose(this.<Response<List<UserBasic>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<UserBasic>>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        adapter.setLoading(false);
                    }

                    @Override
                    public void responseSuccess(@NonNull List<UserBasic> users) {
                        loading = false;
                        adapter.setLoading(false);
                        adapter.addData(users);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                    }
                });
    }

    private void add(Single<Response<Member>> observable) {
        observable.subscribeOn(Schedulers.io())
                .compose(this.<Response<Member>>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<Member>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        String message = getString(R.string.error_failed_to_add_user);
                        if (t instanceof HttpException) {
                            switch (((HttpException) t).code()) {
                                case 409:
                                    message = getString(R.string.error_user_conflict);
                            }
                        }
                        Snackbar.make(root, message, Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void responseSuccess(@NonNull Member member) {
                        Snackbar.make(root, R.string.user_added_successfully, Snackbar.LENGTH_SHORT)
                                .show();
                        dialogAccess.dismiss();
                        dismiss();
                        App.bus().post(new MemberAddedEvent(member));
                    }
                });
    }
}
