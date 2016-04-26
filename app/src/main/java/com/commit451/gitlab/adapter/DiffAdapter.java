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

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    public static final int HEADER_COUNT = 1;

    public interface Listener {
        void onDiffClicked(Diff diff);
    }
    private Listener mListener;
    private RepositoryCommit mRepositoryCommit;
    private ArrayList<Diff> mValues;

    public Diff getValueAt(int position) {
        return mValues.get(position - HEADER_COUNT);
    }

    public DiffAdapter(RepositoryCommit repositoryCommit, Listener listener) {
        mRepositoryCommit = repositoryCommit;
        mListener = listener;
        mValues = new ArrayList<>();
    }

    public void setData(Collection<Diff> diffs) {
        mValues.clear();
        if (diffs != null) {
            mValues.addAll(diffs);
        }
        notifyDataSetChanged();
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onDiffClicked(getValueAt(position));
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
            ((DiffHeaderViewHolder) holder).bind(mRepositoryCommit);
        } else if (holder instanceof DiffViewHolder) {
            Diff diff = getValueAt(position);
            ((DiffViewHolder) holder).bind(diff);
            holder.itemView.setTag(R.id.list_position, position);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size() + HEADER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }
}
