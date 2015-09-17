package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Access denied!
 * Created by Jawn on 9/16/2015.
 */
public class ProjectAccessViewHolder extends RecyclerView.ViewHolder {

    public static ProjectAccessViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project_access, parent, false);
        return new ProjectAccessViewHolder(view);
    }

    @Bind(R.id.textView) TextView title;

    public ProjectAccessViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(String access) {
        title.setText(access);
    }
}
