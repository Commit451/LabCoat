package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.rss.Entry;
import com.commit451.gitlab.viewHolder.FeedEntryViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adapts the feeds
 * Created by John on 10/8/15.
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedEntryViewHolder> {

    public interface Listener {
        void onFeedEntryClicked(Entry entry);
    }
    private Listener mListener;

    private ArrayList<Entry> mValues;

    public FeedAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
    }

    private final View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onFeedEntryClicked(getEntry(position));
        }
    };

    public void setEntries(Collection<Entry> entries) {
        mValues.clear();
        if (entries != null) {
            mValues.addAll(entries);
        }
        notifyDataSetChanged();
    }

    @Override
    public FeedEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FeedEntryViewHolder holder = FeedEntryViewHolder.newInstance(parent);
        holder.itemView.setOnClickListener(mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final FeedEntryViewHolder holder, int position) {
        holder.itemView.setTag(R.id.list_position, position);
        holder.bind(getEntry(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private Entry getEntry(int position) {
        return mValues.get(position);
    }
}
