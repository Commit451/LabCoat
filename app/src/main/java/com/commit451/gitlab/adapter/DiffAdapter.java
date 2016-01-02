package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Diff;
import com.commit451.gitlab.viewHolder.DiffViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Shows a bunch of diffs
 * Created by Jawn on 1/1/2016.
 */
public class DiffAdapter extends RecyclerView.Adapter<DiffViewHolder> {

    public interface Listener {
        void onDiffClicked(Diff diff);
    }
    private Listener mListener;
    private ArrayList<Diff> mValues;

    public Diff getValueAt(int position) {
        return mValues.get(position);
    }

    public DiffAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
    }

    public void setData(Collection<Diff> diffs) {
        mValues.clear();
        if (diffs != null) {
            mValues.addAll(diffs);
            notifyItemRangeInserted(0, diffs.size());
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
    public DiffViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DiffViewHolder holder = DiffViewHolder.newInstance(parent);
        holder.itemView.setOnClickListener(onProjectClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final DiffViewHolder holder, int position) {
        Diff diff = getValueAt(position);
        holder.bind(diff);
        holder.itemView.setTag(R.id.list_position, position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
