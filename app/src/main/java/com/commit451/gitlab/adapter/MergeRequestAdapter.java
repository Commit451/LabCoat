package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.MergeRequest;
import com.commit451.gitlab.viewHolders.MergeRequestViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Merge request adapter!
 * Created by Jawn on 9/20/2015.
 */
public class MergeRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<MergeRequest> mValues;

    public MergeRequest getValueAt(int position) {
        return mValues.get(position);
    }

    public MergeRequestAdapter() {
        mValues = new ArrayList<>();
    }

    public void setData(Collection<MergeRequest> mergeRequests) {
        mValues.clear();
        if (mergeRequests != null) {
            mValues.addAll(mergeRequests);
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MergeRequestViewHolder holder = MergeRequestViewHolder.newInstance(parent);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MergeRequestViewHolder) {
            MergeRequest mergeRequest = getValueAt(position);
            ((MergeRequestViewHolder) holder).bind(mergeRequest);
            holder.itemView.setTag(R.id.list_position, position);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
