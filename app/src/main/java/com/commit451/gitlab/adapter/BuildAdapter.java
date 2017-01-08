package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.viewHolder.BuildViewHolder;
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Builds adapter
 */
public class BuildAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FOOTER_COUNT = 1;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private Listener listener;
    private ArrayList<Build> values;
    private boolean loading = false;

    public BuildAdapter(Listener listener) {
        this.listener = listener;
        values = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                final BuildViewHolder holder = BuildViewHolder.inflate(parent);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        listener.onBuildClicked(getValueAt(position));
                    }
                });
                return holder;
            case TYPE_FOOTER:
                return LoadingFooterViewHolder.inflate(parent);
        }
        throw new IllegalStateException("No holder for view type " + viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BuildViewHolder) {
            Build build = getValueAt(position);
            ((BuildViewHolder) holder).bind(build);
        } else if (holder instanceof LoadingFooterViewHolder) {
            ((LoadingFooterViewHolder) holder).bind(loading);
        } else {
            throw new IllegalStateException("What is this holder?");
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

    public void setValues(Collection<Build> values) {
        this.values.clear();
        addValues(values);
    }

    public void addValues(Collection<Build> values) {
        if (values != null) {
            this.values.addAll(values);
        }
        notifyDataSetChanged();
    }

    public void updateBuild(Build build) {
        int indexToDelete = -1;
        for (int i = 0; i< values.size(); i++) {
            if (values.get(i).getId() == build.getId()) {
                indexToDelete = i;
                break;
            }
        }
        if (indexToDelete != -1) {
            values.remove(indexToDelete);
            values.add(indexToDelete, build);
        }
        notifyItemChanged(indexToDelete);
    }

    public Build getValueAt(int position) {
        return values.get(position);
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyItemChanged(values.size());
    }

    public interface Listener {
        void onBuildClicked(Build build);
    }
}
