package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.tools.Repository;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

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
    @Bind(R.id.project_image) ImageView image;

    public ProjectViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Project project) {
        title.setText(project.getName());
        if (project.equals(Repository.selectedProject)) {
            itemView.setPressed(true);
            itemView.setSelected(true);
            title.setTextColor(itemView.getResources().getColor(R.color.orange));
        } else {
            itemView.setPressed(false);
            itemView.setSelected(false);
            title.setTextColor(itemView.getResources().getColor(R.color.material_blue_grey_800));
        }
        if (!TextUtils.isEmpty(project.getAvatarUrl())) {
            Timber.d("avatar url: " + project.getAvatarUrl());
            Picasso.with(itemView.getContext())
                    .load(project.getAvatarUrl())
                    .into(image);
        }
    }
}
