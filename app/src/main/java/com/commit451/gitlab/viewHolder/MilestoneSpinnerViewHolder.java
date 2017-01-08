package com.commit451.gitlab.viewHolder;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Milestone;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Shows milestone in a spinner
 */
public class MilestoneSpinnerViewHolder extends RecyclerView.ViewHolder {

    public static MilestoneSpinnerViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spinner_milestone, parent, false);
        return new MilestoneSpinnerViewHolder(view);
    }

    @BindView(R.id.title)
    TextView textTitle;

    public MilestoneSpinnerViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(@Nullable Milestone milestone) {
        if (milestone == null) {
            textTitle.setText(R.string.no_milestone);
        } else {
            textTitle.setText(milestone.getTitle());
        }
    }
}
