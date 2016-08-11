package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Branch;
import com.commit451.gitlab.model.api.Label;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Label
 */
public class BranchViewHolder extends RecyclerView.ViewHolder {

    public static BranchViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_branch, parent, false);
        return new BranchViewHolder(view);
    }

    @BindView(R.id.title) public TextView title;

    public BranchViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Branch branch) {
        title.setText(branch.getName());
    }
}