package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.RepositoryCommit;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Header that gives the details of a merge request
 */
public class DiffHeaderViewHolder extends RecyclerView.ViewHolder {

    public static DiffHeaderViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_diff, parent, false);
        return new DiffHeaderViewHolder(view);
    }

    @Bind(R.id.title) TextView mTitle;

    public DiffHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(RepositoryCommit commit) {
        mTitle.setText(commit.getTitle());
    }
}
