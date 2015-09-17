package com.commit451.gitlab.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.ProjectsAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.dialogs.LogoutDialog;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

/**
 * Our very own navigation view
 * Created by Jawn on 7/28/2015.
 */
public class GitLabNavigationView extends FrameLayout{

    @Bind(R.id.profile_image) ImageView profileImage;
    @Bind(R.id.list) RecyclerView projectList;
    ProjectsAdapter mAdapter;

    @OnClick(R.id.drawer_header)
    void onHeaderClick() {
        new LogoutDialog(getContext()).show();
    }

    private final Callback<User> userCallback = new Callback<User>() {

        @Override
        public void onResponse(Response<User> response) {
            if (!response.isSuccess()) {
                return;
            }
            if (getContext() != null) {
                Picasso.with(getContext())
                        .load(response.body().getAvatarUrl())
                        .into(profileImage);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t.toString());
        }
    };

    public GitLabNavigationView(Context context) {
        super(context);
        init();
    }

    public GitLabNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GitLabNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public GitLabNavigationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.nav_drawer, this);
        ButterKnife.bind(this);
        mAdapter = new ProjectsAdapter();
        projectList.setAdapter(mAdapter);
        projectList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void setProjects(List<Project> projects) {
        mAdapter.setData(projects);
    }

    public void setUserId(long userId) {
        GitLabClient.instance().getUser(userId).enqueue(userCallback);
    }
}
