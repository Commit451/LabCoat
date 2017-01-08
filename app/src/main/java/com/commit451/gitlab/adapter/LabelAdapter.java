package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Label;
import com.commit451.gitlab.viewHolder.LabelViewHolder;
import com.commit451.gitlab.viewHolder.ProjectMemberFooterViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Shows a bunch of labels
 */
public class LabelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;

    private Listener listener;

    private ArrayList<Label> items;

    private final View.OnClickListener mProjectMemberClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            LabelViewHolder viewHolder = (LabelViewHolder) v.getTag(R.id.list_view_holder);
            listener.onLabelClicked(getItem(position), viewHolder);
        }
    };

    public LabelAdapter(Listener listener) {
        this.listener = listener;
        items = new ArrayList<>();
    }

    public Label getItem(int position) {
        return items.get(position);
    }

    public void setItems(Collection<Label> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void addLabel(Label label) {
        items.add(0, label);
        notifyItemInserted(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                LabelViewHolder itemViewHolder = LabelViewHolder.inflate(parent);
                itemViewHolder.itemView.setOnClickListener(mProjectMemberClickListener);
                return itemViewHolder;
        }
        throw new IllegalStateException("No idea what to inflate with view type of " + viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProjectMemberFooterViewHolder) {
            //
        } else if (holder instanceof LabelViewHolder) {
            final Label label = getItem(position);
            ((LabelViewHolder) holder).bind(label);
            holder.itemView.setTag(R.id.list_position, position);
            holder.itemView.setTag(R.id.list_view_holder, holder);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    public interface Listener {
        void onLabelClicked(Label label, LabelViewHolder viewHolder);
    }
}
