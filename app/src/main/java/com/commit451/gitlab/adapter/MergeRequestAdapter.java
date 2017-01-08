package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder;
import com.commit451.gitlab.viewHolder.MergeRequestViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Merge request adapter!
 */
public class MergeRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int FOOTER_COUNT = 1;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private Listener listener;
    private List<MergeRequest> values;
    private boolean loading;

    public MergeRequestAdapter(Listener listener) {
        this.listener = listener;
        values = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                MergeRequestViewHolder holder = MergeRequestViewHolder.inflate(parent);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = (int) v.getTag(R.id.list_position);
                        listener.onMergeRequestClicked(getValueAt(position));
                    }
                });
                return holder;
            case TYPE_FOOTER:
                return LoadingFooterViewHolder.inflate(parent);
        }
        throw new IllegalStateException("No holder for type " + viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MergeRequestViewHolder) {
            MergeRequest mergeRequest = getValueAt(position);
            ((MergeRequestViewHolder) holder).bind(mergeRequest);
            holder.itemView.setTag(R.id.list_position, position);
        } else if (holder instanceof LoadingFooterViewHolder) {
            ((LoadingFooterViewHolder) holder).bind(loading);
        }
    }

    @Override
    public int getItemCount() {
        return values.size() + FOOTER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == values.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    public MergeRequest getValueAt(int position) {
        return values.get(position);
    }

    public void setData(Collection<MergeRequest> mergeRequests) {
        values.clear();
        addData(mergeRequests);
    }

    public void addData(Collection<MergeRequest> mergeRequests) {
        if (mergeRequests != null) {
            values.addAll(mergeRequests);
        }
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyItemChanged(values.size());
    }

    public interface Listener {
        void onMergeRequestClicked(MergeRequest mergeRequest);
    }
}
