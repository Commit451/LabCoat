package com.commit451.gitlab.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Ref;
import com.commit451.gitlab.model.api.Tag;
import com.commit451.gitlab.viewHolder.TagViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Tags
 */
public class TagAdapter extends RecyclerView.Adapter<TagViewHolder> {

    private Listener listener;

    private ArrayList<Tag> values;
    @Nullable
    private Ref ref;

    public TagAdapter(@Nullable Ref ref, Listener listener) {
        this.listener = listener;
        values = new ArrayList<>();
        this.ref = ref;
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TagViewHolder holder = TagViewHolder.inflate(parent);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag(R.id.list_position);
                listener.onTagClicked(getEntry(position));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final TagViewHolder holder, int position) {
        holder.itemView.setTag(R.id.list_position, position);
        Tag tag = getEntry(position);
        boolean selected = false;
        if (ref != null) {
            if (ref.getType() == Ref.TYPE_TAG
                    && ref.getRef().equals(tag.getName())) {
                selected = true;
            }
        }
        holder.bind(tag, selected);
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public void setEntries(Collection<Tag> entries) {
        values.clear();
        if (entries != null) {
            values.addAll(entries);
        }
        notifyDataSetChanged();
    }

    private Tag getEntry(int position) {
        return values.get(position);
    }

    public interface Listener {
        void onTagClicked(Tag entry);
    }
}
