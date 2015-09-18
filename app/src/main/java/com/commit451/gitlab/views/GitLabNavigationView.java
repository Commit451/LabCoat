package com.commit451.gitlab.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
import fr.tkeunebr.gravatar.Gravatar;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

/**
 * Our very own navigation view
 * Created by Jawn on 7/28/2015.
 */
public class GitLabNavigationView extends FrameLayout{

    @Bind(R.id.profile_image) ImageView profileImage;
    @Bind(R.id.profile_user) TextView userName;
    @Bind(R.id.profile_email) TextView userEmail;
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
            bindUser(response.body());
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

    public void loadCurrentUser() {
        GitLabClient.instance().getUser().enqueue(userCallback);
    }

    private void bindUser(User user) {
        if (getContext() == null) {
            return;
        }
        if (user.getUsername() != null) {
            userName.setText(user.getUsername());
        }
        int size = getResources().getDimensionPixelSize(R.dimen.larger_image_size);
        String url = "http://www.gravatar.com/avatar/00000000000000000000000000000000?s=" + size;
        if(user.getEmail() != null) {
            url = Gravatar.init().with(user.getEmail()).size(size).build();
            userEmail.setText(user.getEmail());
        }
        Picasso.with(getContext())
                .load(url)
                .into(profileImage);
    }
}
