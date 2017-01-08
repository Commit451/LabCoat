package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.viewHolder.CommitViewHolder;
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Shows a list of commits to a project, seen in a project overview
 */
public class CommitAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FOOTER_COUNT = 1;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private Listener listener;
    private ArrayList<RepositoryCommit> values;
    private boolean loading = false;

    public CommitAdapter(Listener listener) {
        this.listener = listener;
        values = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                CommitViewHolder holder = CommitViewHolder.inflate(parent);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = (int) v.getTag(R.id.list_position);
                        listener.onCommitClicked(getValueAt(position));
                    }
                });
                return holder;
            case TYPE_FOOTER:
                return LoadingFooterViewHolder.inflate(parent);
        }
        throw new IllegalStateException("No known ViewHolder for type " + viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommitViewHolder) {
            RepositoryCommit commit = getValueAt(position);
            ((CommitViewHolder) holder).bind(commit);
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

    public RepositoryCommit getValueAt(int position) {
        return values.get(position);
    }

    public void addData(Collection<RepositoryCommit> commits) {
        if (commits != null) {
            values.addAll(commits);
            notifyItemRangeInserted(0, commits.size());
        }
        notifyDataSetChanged();
    }

    public void setData(Collection<RepositoryCommit> commits) {
        values.clear();
        addData(commits);
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyItemChanged(values.size());
    }

    public interface Listener {
        void onCommitClicked(RepositoryCommit commit);
    }
}

