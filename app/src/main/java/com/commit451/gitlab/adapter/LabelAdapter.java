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
 * Shows a projects members and a groups members
 */
public class LabelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private static final int FOOTER_COUNT = 1;

    private Listener mListener;

    private ArrayList<Label> mItems;

    private final View.OnClickListener mProjectMemberClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            LabelViewHolder viewHolder = (LabelViewHolder) v.getTag(R.id.list_view_holder);
            mListener.onLabelClicked(getItem(position), viewHolder);
        }
    };

    private final View.OnClickListener mFooterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onAddLabelClicked();
        }
    };

    public LabelAdapter(Listener listener) {
        mListener = listener;
        mItems = new ArrayList<>();
    }

    public Label getItem(int position) {
        return mItems.get(position);
    }

    public void setItems(Collection<Label> data) {
        mItems.clear();
        if (data != null) {
            mItems.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void addLabel(Label label) {
        mItems.add(label);
        notifyItemInserted(mItems.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                LabelViewHolder itemViewHolder = LabelViewHolder.inflate(parent);
                itemViewHolder.itemView.setOnClickListener(mProjectMemberClickListener);
                return itemViewHolder;
            case TYPE_FOOTER:
                ProjectMemberFooterViewHolder footerHolder = ProjectMemberFooterViewHolder.inflate(parent);
                footerHolder.itemView.setOnClickListener(mFooterClickListener);
                return footerHolder;
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
        return mItems.size() + FOOTER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mItems.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    public interface Listener {
        void onLabelClicked(Label label, LabelViewHolder viewHolder);
        void onAddLabelClicked();
    }
}
