package com.commit451.gitlab.viewHolders;

import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Project;
import com.github.ivbaranov.mli.MaterialLetterIcon;

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

    @Bind(R.id.project_image) ImageView image;
    @Bind(R.id.project_letter) MaterialLetterIcon icon;
    @Bind(R.id.project_title) TextView title;
    @Bind(R.id.project_description) TextView description;
    @Bind(R.id.project_stars) TextView stars;
    @Bind(R.id.project_forks) TextView forks;

    public ProjectViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Project project, int color) {
        if (project.getAvatarUrl() == null || project.getAvatarUrl().equals(Uri.EMPTY)) {
            image.setVisibility(View.GONE);
            icon.setVisibility(View.VISIBLE);
            icon.setLetter(project.getName().substring(0, 1));
            icon.setLetterColor(Color.WHITE);
            icon.setShapeColor(color);
        } else {
            image.setVisibility(View.VISIBLE);
            icon.setVisibility(View.GONE);
            GitLabClient.getPicasso()
                    .load(project.getAvatarUrl())
                    .into(image);
        }
        title.setText(project.getNameWithNamespace());
        if (!TextUtils.isEmpty(project.getDescription())) {
            description.setVisibility(View.VISIBLE);
            description.setText(project.getDescription());
        } else {
            description.setVisibility(View.GONE);
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
