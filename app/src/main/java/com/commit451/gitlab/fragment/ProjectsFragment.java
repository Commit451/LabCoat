package com.commit451.gitlab.fragment;

import android.content.Context;
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
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.adapter.ProjectAdapter;
import com.commit451.gitlab.api.GitLab;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.util.LinkHeaderParser;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

public class ProjectsFragment extends ButterKnifeFragment {

    private static final String EXTRA_MODE = "extra_mode";
    private static final String EXTRA_QUERY = "extra_query";
    private static final String EXTRA_GROUP = "extra_group";

    public static final int MODE_ALL = 0;
    public static final int MODE_MINE = 1;
    public static final int MODE_STARRED = 2;
    public static final int MODE_SEARCH = 3;
    public static final int MODE_GROUP = 4;

    public static ProjectsFragment newInstance(int mode) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_MODE, mode);

        ProjectsFragment fragment = new ProjectsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ProjectsFragment newInstance(String searchTerm) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_MODE, MODE_SEARCH);
        args.putString(EXTRA_QUERY, searchTerm);
        ProjectsFragment fragment = new ProjectsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ProjectsFragment newInstance(Group group) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_MODE, MODE_GROUP);
        args.putParcelable(EXTRA_GROUP, Parcels.wrap(group));
        ProjectsFragment fragment = new ProjectsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listProjects;
    @BindView(R.id.message_text)
    TextView textMessage;

    LinearLayoutManager layoutManagerProjects;
    ProjectAdapter adapterProjects;

    int mode;
    String query;
    Uri nextPageUrl;
    boolean loading = false;
    Listener listener;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManagerProjects.getChildCount();
            int totalItemCount = layoutManagerProjects.getItemCount();
            int firstVisibleItem = layoutManagerProjects.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore();
            }
        }
    };

    private final ProjectAdapter.Listener mProjectsListener = new ProjectAdapter.Listener() {
        @Override
        public void onProjectClicked(Project project) {
            if (listener == null) {
                Navigator.navigateToProject(getActivity(), project);
            } else {
                listener.onProjectClicked(project);
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getArguments().getInt(EXTRA_MODE);
        query = getArguments().getString(EXTRA_QUERY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_projects, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapterProjects = new ProjectAdapter(getActivity(), mProjectsListener);
        layoutManagerProjects = new LinearLayoutManager(getActivity());
        listProjects.setLayoutManager(layoutManagerProjects);
        listProjects.addItemDecoration(new DividerItemDecoration(getActivity()));
        listProjects.setAdapter(adapterProjects);
        listProjects.addOnScrollListener(mOnScrollListener);

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
            case MODE_ALL:
                showLoading();
                actuallyLoadIt(getGitLab().getAllProjects());
                break;
            case MODE_MINE:
                showLoading();
                actuallyLoadIt(getGitLab().getMyProjects());
                break;
            case MODE_STARRED:
                showLoading();
                actuallyLoadIt(getGitLab().getStarredProjects());
                break;
            case MODE_SEARCH:
                if (query != null) {
                    showLoading();
                    actuallyLoadIt(getGitLab().searchAllProjects(query));
                }
                break;
            case MODE_GROUP:
                showLoading();
                Group group = Parcels.unwrap(getArguments().getParcelable(EXTRA_GROUP));
                if (group == null) {
                    throw new IllegalStateException("You must also pass a group if you want to show a groups projects");
                }
                actuallyLoadIt(getGitLab().getGroupProjects(group.getId()));
                break;
            default:
                throw new IllegalStateException(mode + " is not defined");
        }
    }

    private void actuallyLoadIt(Single<Response<List<Project>>> observable) {
        observable.compose(this.<Response<List<Project>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Project>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        loading = false;
                        Timber.e(e);
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.connection_error);
                        adapterProjects.setData(null);
                        nextPageUrl = null;
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Project> projects) {
                        loading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        if (projects.isEmpty()) {
                            textMessage.setVisibility(View.VISIBLE);
                            textMessage.setText(R.string.no_projects);
                        }
                        adapterProjects.setData(projects);
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
        adapterProjects.setLoading(true);
        Timber.d("loadMore called for %s", nextPageUrl);
        getGitLab().getProjects(nextPageUrl.toString())
                .compose(this.<Response<List<Project>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Project>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        loading = false;
                        Timber.e(e);
                        adapterProjects.setLoading(false);
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Project> projects) {
                        loading = false;
                        adapterProjects.setLoading(false);
                        adapterProjects.addData(projects);
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

    public void searchQuery(String query) {
        this.query = query;

        if (adapterProjects != null) {
            adapterProjects.clearData();
            loadData();
        }
    }

    private GitLab getGitLab() {
        if (listener != null) {
            return listener.getGitLab();
        } else {
            return App.get().getGitLab();
        }
    }

    public interface Listener {
        void onProjectClicked(Project project);

        GitLab getGitLab();
    }
}
