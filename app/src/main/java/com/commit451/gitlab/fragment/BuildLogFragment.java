package com.commit451.gitlab.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.event.BuildChangedEvent;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.BuildUtil;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import butterknife.BindView;
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

    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.log) TextView mTextLog;
    @BindView(R.id.message_text) TextView mMessageView;

    Project mProject;
    Build mBuild;

    EventReceiver mEventReceiver;

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
        mEventReceiver = new EventReceiver();
        App.bus().register(mEventReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.bus().unregister(mEventReceiver);
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

        String url = BuildUtil.getRawBuildUrl(App.instance().getAccount().getServerUrl().toString(), mProject, mBuild);

        App.instance().getGitLab().getRaw(url).enqueue(new EasyCallback<String>() {
            @Override
            public void success(@NonNull String response) {
                if (getView() == null) {
                    return;
                }
                mSwipeRefreshLayout.setRefreshing(false);
                mTextLog.setText(response);
            }

            @Override
            public void failure(Throwable t) {
                Timber.e(t);
                if (getView() == null) {
                    return;
                }
                mSwipeRefreshLayout.setRefreshing(false);
                mMessageView.setVisibility(View.VISIBLE);
            }
        });
    }

    private class EventReceiver {

        @Subscribe
        public void onBuildChanged(BuildChangedEvent event) {
            if (mBuild.getId() == event.build.getId()) {
                mBuild = event.build;
                loadData();
            }
        }
    }
}