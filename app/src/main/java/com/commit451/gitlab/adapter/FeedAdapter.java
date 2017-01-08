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
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedEntryViewHolder> {

    Listener listener;
    ArrayList<Entry> values;

    public FeedAdapter(Listener listener) {
        this.listener = listener;
        values = new ArrayList<>();
    }

    public void setEntries(Collection<Entry> entries) {
        values.clear();
        if (entries != null) {
            values.addAll(entries);
        }
        notifyDataSetChanged();
    }

    @Override
    public FeedEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final FeedEntryViewHolder holder = FeedEntryViewHolder.inflate(parent);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                listener.onFeedEntryClicked(getEntry(position));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final FeedEntryViewHolder holder, int position) {
        holder.itemView.setTag(R.id.list_position, position);
        holder.bind(getEntry(position));
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    private Entry getEntry(int position) {
        return values.get(position);
    }

    public interface Listener {
        void onFeedEntryClicked(Entry entry);
    }
}
