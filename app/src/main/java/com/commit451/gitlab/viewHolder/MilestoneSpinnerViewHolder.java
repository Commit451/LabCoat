package com.commit451.gitlab.viewHolder;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.util.AppThemeUtil;

import butterknife.Bind;
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

    @Bind(R.id.title)
    TextView mTitle;

    public MilestoneSpinnerViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(@Nullable Milestone milestone) {
        if (milestone == null) {
            mTitle.setText(R.string.no_milestone);
        } else {
            mTitle.setText(milestone.getTitle());
        }
    }
}
