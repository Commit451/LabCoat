package com.commit451.gitlab.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.event.BuildChangedEvent;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.util.BuildUtil;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Shows the build artifacts
 */
public class BuildLogFragment extends ButterKnifeFragment {

    private static final String KEY_PROJECT = "project";
    private static final String KEY_BUILD = "build";

    public static BuildLogFragment newInstance(Project project, Build build) {
        BuildLogFragment fragment = new BuildLogFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_PROJECT, Parcels.wrap(project));
        args.putParcelable(KEY_BUILD, Parcels.wrap(build));
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.log)
    TextView mTextLog;
    @BindView(R.id.message_text)
    TextView mMessageView;

    Project mProject;
    Build mBuild;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProject = Parcels.unwrap(getArguments().getParcelable(KEY_PROJECT));
        mBuild = Parcels.unwrap(getArguments().getParcelable(KEY_BUILD));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_build_log, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        loadData();
        App.bus().register(this);
    }

    @Override
    public void onDestroyView() {
        App.bus().unregister(this);
        super.onDestroyView();
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        String url = BuildUtil.getRawBuildUrl(App.get().getAccount().getServerUrl(), mProject, mBuild);

        App.get().getGitLab().getRaw(url)
                .compose(this.<String>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<String>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        mSwipeRefreshLayout.setRefreshing(false);
                        mMessageView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void success(@NonNull String s) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mTextLog.setText(s);
                    }
                });
    }

    @Subscribe
    public void onBuildChanged(BuildChangedEvent event) {
        if (mBuild.getId() == event.build.getId()) {
            mBuild = event.build;
            loadData();
        }
    }
}