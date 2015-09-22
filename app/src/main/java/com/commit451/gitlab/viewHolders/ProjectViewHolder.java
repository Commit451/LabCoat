package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Project;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Projects, yay!
 * Created by Jawn on 6/11/2015.
 */
public class ProjectViewHolder extends RecyclerView.ViewHolder {

    public static ProjectViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Bind(R.id.project_title) TextView title;
    @Bind(R.id.project_description) TextView description;
    @Bind(R.id.project_stars) TextView stars;
    @Bind(R.id.project_forks) TextView forks;

    public ProjectViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Project project) {
        title.setText(project.getName());
        if (project.getDescription() != null) {
            description.setText(project.getDescription());
        } else {
            description.setText("");
        }
        if (project.getStarCount() == null) {
            stars.setText("0");
        } else {
            stars.setText(project.getStarCount() + "");
        }
        if (project.getForksCount() == null) {
            forks.setText("0");
        } else {
            forks.setText(project.getForksCount() + "");
        }
    }
}
