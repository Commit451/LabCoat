package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.util.DateUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Header with information for milestones
 */
public class MilestoneHeaderViewHolder extends RecyclerView.ViewHolder {

    public static MilestoneHeaderViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_milestone, parent, false);
        return new MilestoneHeaderViewHolder(view);
    }

    @BindView(R.id.description)
    TextView mDescriptionView;
    @BindView(R.id.due_date)
    TextView mDueDateView;

    public MilestoneHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Milestone milestone) {
        if (milestone.getDescription() != null) {
            mDescriptionView.setText(milestone.getDescription());
        }
        if (milestone.getDueDate() != null) {
            CharSequence due = DateUtil.getRelativeTimeSpanString(itemView.getContext(), milestone.getDueDate());
            mDueDateView.setText(String.format(itemView.getResources().getString(R.string.due_date_formatted), due));
        }
    }
}
