package com.commit451.gitlab.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Ref;
import com.commit451.gitlab.model.api.Branch;
import com.commit451.gitlab.viewHolder.BranchViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adapts the feeds
 */
public class BranchAdapter extends RecyclerView.Adapter<BranchViewHolder> {


    private Listener listener;

    private ArrayList<Branch> values;
    @Nullable
    private Ref ref;

    public BranchAdapter(@Nullable Ref currentRef, Listener listener) {
        this.listener = listener;
        values = new ArrayList<>();
        ref = currentRef;
    }

    private final View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            listener.onBranchClicked(getEntry(position));
        }
    };

    public void setEntries(Collection<Branch> entries) {
        values.clear();
        if (entries != null) {
            values.addAll(entries);
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
        Branch branch = getEntry(position);
        boolean selected = false;
        if (ref != null) {
            if (ref.getType() == Ref.TYPE_BRANCH
                    && ref.getRef().equals(branch.getName())) {
                selected = true;
            }
        }
        holder.bind(branch, selected);
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    private Branch getEntry(int position) {
        return values.get(position);
    }

    public interface Listener {
        void onBranchClicked(Branch entry);
    }
}
