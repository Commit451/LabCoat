package com.commit451.gitlab.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.ProjectsAdapter;
import com.commit451.gitlab.model.Project;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Our very own navigation view
 * Created by Jawn on 7/28/2015.
 */
public class GitLabNavigationView extends FrameLayout{

    @Bind(R.id.list) RecyclerView projectList;

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
        projectList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void setProjects(List<Project> projects) {
        projectList.setAdapter(new ProjectsAdapter(projects));
    }
}
