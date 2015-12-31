package com.commit451.gitlab.viewHolder;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Project;
import com.github.ivbaranov.mli.MaterialLetterIcon;

import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    @Bind(R.id.project_image) ImageView mImageView;
    @Bind(R.id.project_letter) MaterialLetterIcon mLetterView;
    @Bind(R.id.project_title) TextView mTitleView;
    @Bind(R.id.project_description) TextView mDescriptionView;
    @Bind(R.id.project_stars) TextView mStarsView;
    @Bind(R.id.project_forks) TextView mForksView;
    @Bind(R.id.project_visibility) ImageView mVisibilityView;

    public ProjectViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Project project, int color) {
        if (project.getAvatarUrl() != null && !project.getAvatarUrl().equals(Uri.EMPTY)) {
            mLetterView.setVisibility(View.GONE);

            mImageView.setVisibility(View.VISIBLE);
            GitLabClient.getPicasso()
                    .load(project.getAvatarUrl())
                    .into(mImageView);
        } else {
            mImageView.setVisibility(View.GONE);

            mLetterView.setVisibility(View.VISIBLE);
            mLetterView.setLetter(project.getName().substring(0, 1));
            mLetterView.setLetterColor(Color.WHITE);
            mLetterView.setShapeColor(color);
        }

        mTitleView.setText(project.getNameWithNamespace());
        if (!TextUtils.isEmpty(project.getDescription())) {
            mDescriptionView.setVisibility(View.VISIBLE);
            mDescriptionView.setText(project.getDescription());
        } else {
            mDescriptionView.setVisibility(View.GONE);
            mDescriptionView.setText("");
        }

        mStarsView.setText(project.getStarCount() + "");
        mForksView.setText(project.getForksCount() + "");

        if (project.isPublic()) {
            mVisibilityView.setImageResource(R.drawable.ic_public_24dp);
        } else {
            mVisibilityView.setImageResource(R.drawable.ic_private_24dp);
        }
    }
}
