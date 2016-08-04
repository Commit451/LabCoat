package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.TodoAdapter;
import com.commit451.gitlab.model.api.Todo;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.util.PaginationUtil;

import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

public class TodoFragment extends ButterKnifeFragment {

    private static final String EXTRA_MODE = "extra_mode";

    public static final int MODE_TODO = 0;
    public static final int MODE_DONE = 1;

    public static TodoFragment newInstance(int mode) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_MODE, mode);

        TodoFragment fragment = new TodoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView mProjectsListView;
    @BindView(R.id.message_text)
    TextView mMessageView;

    LinearLayoutManager mLayoutManager;
    TodoAdapter mTodoAdapter;

    int mMode;
    Uri mNextPageUrl;
    boolean mLoading = false;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMore();
            }
        }
    };

    private final EasyCallback<List<Todo>> mProjectsCallback = new EasyCallback<List<Todo>>() {
        @Override
        public void success(@NonNull List<Todo> response) {
            mLoading = false;
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (response.isEmpty()) {
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_todos);
            }
            mTodoAdapter.setData(response);
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
            Timber.d("Next page url " + mNextPageUrl);
        }

        @Override
        public void failure(Throwable t) {
            mLoading = false;
            Timber.e(t, null);
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.connection_error);
            mTodoAdapter.setData(null);
            mNextPageUrl = null;
        }
    };

    private final EasyCallback<List<Todo>> mMoreProjectsCallback = new EasyCallback<List<Todo>>() {
        @Override
        public void success(@NonNull List<Todo> response) {
            mLoading = false;
            if (getView() == null) {
                return;
            }
            mTodoAdapter.setLoading(false);
            mTodoAdapter.addData(response);
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
            Timber.d("Next page url " + mNextPageUrl);
        }

        @Override
        public void failure(Throwable t) {
            mLoading = false;
            Timber.e(t, null);

            if (getView() == null) {
                return;
            }
            mTodoAdapter.setLoading(false);
        }
    };

    private final TodoAdapter.Listener mProjectsListener = new TodoAdapter.Listener() {

        @Override
        public void onTodoClicked(Todo todo) {
            Navigator.navigateToUrl(getActivity(), Uri.parse(todo.getTargetUrl()), App.instance().getAccount());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMode = getArguments().getInt(EXTRA_MODE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTodoAdapter = new TodoAdapter(mProjectsListener);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mProjectsListView.setLayoutManager(mLayoutManager);
        mProjectsListView.setAdapter(mTodoAdapter);
        mProjectsListView.addOnScrollListener(mOnScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        loadData();
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }
        mMessageView.setVisibility(View.GONE);

        mNextPageUrl = null;

        switch (mMode) {
            case MODE_TODO:
                showLoading();
                App.instance().getGitLab().getTodos(Todo.STATE_PENDING).enqueue(mProjectsCallback);
                break;
            case MODE_DONE:
                showLoading();
                App.instance().getGitLab().getTodos(Todo.STATE_DONE).enqueue(mProjectsCallback);
                break;
            default:
                throw new IllegalStateException(mMode + " is not defined");
        }
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

        if (mNextPageUrl == null) {
            return;
        }
        mLoading = true;
        mTodoAdapter.setLoading(true);
        Timber.d("loadMore called for " + mNextPageUrl);
        App.instance().getGitLab().getTodosByUrl(mNextPageUrl.toString()).enqueue(mMoreProjectsCallback);
    }

    private void showLoading() {
        mLoading = true;
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
    }
}
