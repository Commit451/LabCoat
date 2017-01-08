package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.easel.Easel;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Branch;

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

    @BindView(R.id.title)
    public TextView textTitle;

    int colorHighlighted;

    public BranchViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        colorHighlighted = Easel.getThemeAttrColor(itemView.getContext(), R.attr.colorControlHighlight);
    }

    public void bind(Branch branch, boolean selected) {
        textTitle.setText(branch.getName());
        if (selected) {
            itemView.setBackgroundColor(colorHighlighted);
        } else {
            itemView.setBackground(null);
        }
    }
}