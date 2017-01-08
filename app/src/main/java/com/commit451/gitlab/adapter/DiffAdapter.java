package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Diff;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.viewHolder.DiffHeaderViewHolder;
import com.commit451.gitlab.viewHolder.DiffViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Shows a bunch of diffs
 */
public class DiffAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private static final int HEADER_COUNT = 1;

    private Listener listener;
    private RepositoryCommit repositoryCommit;
    private ArrayList<Diff> values;

    public Diff getValueAt(int position) {
        return values.get(position - HEADER_COUNT);
    }

    public DiffAdapter(RepositoryCommit repositoryCommit, Listener listener) {
        this.repositoryCommit = repositoryCommit;
        this.listener = listener;
        values = new ArrayList<>();
    }

    public void setData(Collection<Diff> diffs) {
        values.clear();
        if (diffs != null) {
            values.addAll(diffs);
        }
        notifyDataSetChanged();
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            listener.onDiffClicked(getValueAt(position));
        }
    };

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return DiffHeaderViewHolder.inflate(parent);
            case TYPE_ITEM:
                DiffViewHolder holder = DiffViewHolder.inflate(parent);
                holder.itemView.setOnClickListener(onProjectClickListener);
                return holder;
        }
        throw new IllegalStateException("No known view holder for " + viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DiffHeaderViewHolder) {
            ((DiffHeaderViewHolder) holder).bind(repositoryCommit);
        } else if (holder instanceof DiffViewHolder) {
            Diff diff = getValueAt(position);
            ((DiffViewHolder) holder).bind(diff);
            holder.itemView.setTag(R.id.list_position, position);
        }
    }

    @Override
    public int getItemCount() {
        return values.size() + HEADER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    public interface Listener {
        void onDiffClicked(Diff diff);
    }
}
