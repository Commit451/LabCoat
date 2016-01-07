package com.commit451.gitlab.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.viewHolder.MilestoneSpinnerViewHolder;

import java.util.List;

/**
 * Adapter to show assignees in a spinner
 */
public class MilestoneSpinnerAdapter extends ArrayAdapter<Milestone> {

    public MilestoneSpinnerAdapter(Context context, List<Milestone> milestones) {
        super(context, 0, milestones);
        milestones.add(0, null);
        notifyDataSetChanged();
    }

    public int getSelectedItemPosition(Milestone currentMilestone) {
        if (currentMilestone == null) {
            return 0;
        }
        for (int i=0; i<getCount(); i++) {
            Milestone milestone = getItem(i);
            if (milestone != null && currentMilestone.getId() == milestone.getId()) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getTheView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getTheView(position, convertView, parent);
    }

    private View getTheView(int position, View convertView, ViewGroup parent) {
        Milestone milestone = getItem(position);
        MilestoneSpinnerViewHolder milestoneSpinnerViewHolder;
        if (convertView == null) {
            milestoneSpinnerViewHolder = MilestoneSpinnerViewHolder.newInstance(parent);
            milestoneSpinnerViewHolder.itemView.setTag(R.id.list_view_holder, milestoneSpinnerViewHolder);
        } else {
            milestoneSpinnerViewHolder = (MilestoneSpinnerViewHolder) convertView.getTag(R.id.list_view_holder);
        }
        milestoneSpinnerViewHolder.bind(milestone);
        return milestoneSpinnerViewHolder.itemView;
    }

}