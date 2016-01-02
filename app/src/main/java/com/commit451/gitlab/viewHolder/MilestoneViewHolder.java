package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.util.DateUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MilestoneViewHolder extends RecyclerView.ViewHolder {

    public static MilestoneViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_milestone, parent, false);
        return new MilestoneViewHolder(view);
    }

    @Bind(R.id.title) TextView mTitleView;
    @Bind(R.id.due_date) TextView mDueDateView;

    public MilestoneViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Milestone milestone) {
        mTitleView.setText(milestone.getTitle());
        if (milestone.getDueDate() != null) {
            mDueDateView.setVisibility(View.VISIBLE);
            mDueDateView.setText(DateUtils.getRelativeTimeSpanString(itemView.getContext(), milestone.getDueDate()));
        } else {
            mDueDateView.setVisibility(View.GONE);
            mDueDateView.setText("");
        }
    }
}