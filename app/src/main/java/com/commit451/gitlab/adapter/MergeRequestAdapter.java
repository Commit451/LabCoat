package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
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
    public interface Listener {
        void onMergeRequestClicked(MergeRequest mergeRequest);
    }
    private Listener mListener;
    private List<MergeRequest> mValues;

    private final View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onMergeRequestClicked(getValueAt(position));
        }
    };

    public MergeRequestAdapter(Listener listener) {
        mListener = listener;
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
        holder.itemView.setOnClickListener(mOnItemClickListener);
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

    public MergeRequest getValueAt(int position) {
        return mValues.get(position);
    }
}
