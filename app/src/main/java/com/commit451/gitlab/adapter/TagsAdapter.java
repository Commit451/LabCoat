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
 * Adapts the feeds
 */
public class TagsAdapter extends RecyclerView.Adapter<TagViewHolder> {

    public interface Listener {
        void onTagClicked(Tag entry);
    }
    private Listener mListener;

    private ArrayList<Tag> mValues;
    @Nullable
    private Ref mRef;

    public TagsAdapter(@Nullable Ref ref, Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
        mRef = ref;
    }

    private final View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onTagClicked(getEntry(position));
        }
    };

    public void setEntries(Collection<Tag> entries) {
        mValues.clear();
        if (entries != null) {
            mValues.addAll(entries);
        }
        notifyDataSetChanged();
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TagViewHolder holder = TagViewHolder.inflate(parent);
        holder.itemView.setOnClickListener(mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final TagViewHolder holder, int position) {
        holder.itemView.setTag(R.id.list_position, position);
        Tag tag = getEntry(position);
        boolean selected = false;
        if (mRef != null) {
            if (mRef.getType() == Ref.TYPE_TAG
                    && mRef.getRef().equals(tag.getName())) {
                selected = true;
            }
        }
        holder.bind(tag, selected);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private Tag getEntry(int position) {
        return mValues.get(position);
    }
}
