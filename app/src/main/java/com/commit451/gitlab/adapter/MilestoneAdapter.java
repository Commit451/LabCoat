package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.viewHolder.MilestoneViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class MilestoneAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public interface Listener {
        void onMilestoneClicked(Milestone milestone);
    }
    private Listener mListener;
    private List<Milestone> mValues;

    private final View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onMilestoneClicked(getValueAt(position));
        }
    };

    public MilestoneAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
    }

    public void setData(Collection<Milestone> milestones) {
        mValues.clear();
        if (milestones != null) {
            mValues.addAll(milestones);
        }
        notifyDataSetChanged();
    }

    public void addMilestone(Milestone milestone) {
        mValues.add(0, milestone);
        notifyItemInserted(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MilestoneViewHolder holder = MilestoneViewHolder.newInstance(parent);
        holder.itemView.setOnClickListener(mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MilestoneViewHolder) {
            Milestone milestone = getValueAt(position);
            ((MilestoneViewHolder) holder).bind(milestone);
            holder.itemView.setTag(R.id.list_position, position);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public Milestone getValueAt(int position) {
        return mValues.get(position);
    }
}