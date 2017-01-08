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

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.TodoAdapter;
import com.commit451.gitlab.model.api.Todo;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.util.LinkHeaderParser;

import java.util.List;

import butterknife.BindView;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
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
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listTodos;
    @BindView(R.id.message_text)
    TextView textMessage;

    LinearLayoutManager layoutManagerTodos;
    TodoAdapter adapterTodos;

    int mode;
    Uri nextPageUrl;
    boolean loading = false;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManagerTodos.getChildCount();
            int totalItemCount = layoutManagerTodos.getItemCount();
            int firstVisibleItem = layoutManagerTodos.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getArguments().getInt(EXTRA_MODE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapterTodos = new TodoAdapter(new TodoAdapter.Listener() {
            @Override
            public void onTodoClicked(Todo todo) {
                Navigator.navigateToUrl(getActivity(), Uri.parse(todo.getTargetUrl()), App.get().getAccount());
            }
        });
        layoutManagerTodos = new LinearLayoutManager(getActivity());
        listTodos.setLayoutManager(layoutManagerTodos);
        listTodos.setAdapter(adapterTodos);
        listTodos.addOnScrollListener(onScrollListener);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        textMessage.setVisibility(View.GONE);

        nextPageUrl = null;

        switch (mode) {
            case MODE_TODO:
                showLoading();
                getTodos(App.get().getGitLab().getTodos(Todo.STATE_PENDING));
                break;
            case MODE_DONE:
                showLoading();
                getTodos(App.get().getGitLab().getTodos(Todo.STATE_DONE));
                break;
            default:
                throw new IllegalStateException(mode + " is not defined");
        }
    }

    private void getTodos(Single<Response<List<Todo>>> observable) {
        observable
                .compose(this.<Response<List<Todo>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Todo>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        loading = false;
                        Timber.e(e);
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.connection_error);
                        adapterTodos.setData(null);
                        nextPageUrl = null;
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Todo> todos) {
                        loading = false;

                        swipeRefreshLayout.setRefreshing(false);
                        if (todos.isEmpty()) {
                            textMessage.setVisibility(View.VISIBLE);
                            textMessage.setText(R.string.no_todos);
                        }
                        adapterTodos.setData(todos);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        Timber.d("Next page url " + nextPageUrl);
                    }
                });
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

        if (nextPageUrl == null) {
            return;
        }
        loading = true;
        adapterTodos.setLoading(true);
        Timber.d("loadMore called for " + nextPageUrl);
        App.get().getGitLab().getTodosByUrl(nextPageUrl.toString())
                .compose(this.<Response<List<Todo>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Todo>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        loading = false;
                        Timber.e(e);
                        adapterTodos.setLoading(false);
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Todo> todos) {
                        loading = false;
                        adapterTodos.setLoading(false);
                        adapterTodos.addData(todos);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        Timber.d("Next page url " + nextPageUrl);
                    }
                });
    }

    private void showLoading() {
        loading = true;
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });
    }
}
