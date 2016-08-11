package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Branch;
import com.commit451.gitlab.model.rss.Entry;
import com.commit451.gitlab.viewHolder.BranchViewHolder;
import com.commit451.gitlab.viewHolder.FeedEntryViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adapts the feeds
 */
public class BranchesAdapter extends RecyclerView.Adapter<BranchViewHolder> {

    public interface Listener {
        void onBranchClicked(Branch entry);
    }
    private Listener mListener;

    private ArrayList<Branch> mValues;

    public BranchesAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
    }

    private final View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onBranchClicked(getEntry(position));
        }
    };

    public void setEntries(Collection<Branch> entries) {
        mValues.clear();
        if (entries != null) {
            mValues.addAll(entries);
        }
        notifyDataSetChanged();
    }

    @Override
    public BranchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BranchViewHolder holder = BranchViewHolder.inflate(parent);
        holder.itemView.setOnClickListener(mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final BranchViewHolder holder, int position) {
        holder.itemView.setTag(R.id.list_position, position);
        holder.bind(getEntry(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private Branch getEntry(int position) {
        return mValues.get(position);
    }
}
