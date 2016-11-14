package com.commit451.gitlab.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.model.api.Label;
import com.commit451.gitlab.viewHolder.AddLabelViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * So many labels
 */
public class AddIssueLabelAdapter extends RecyclerView.Adapter<AddLabelViewHolder> {

    private ArrayList<Label> mValues;
    private Listener mListener;

    public AddIssueLabelAdapter(Listener listener) {
        mValues = new ArrayList<>();
        mListener = listener;
    }

    public void setLabels(Collection<Label> labels) {
        mValues.clear();
        addLabels(labels);
    }

    public void addLabels(Collection<Label> labels) {
        if (labels != null) {
            mValues.addAll(labels);
        }
        notifyDataSetChanged();
    }

    public void addLabel(Label label) {
        mValues.add(label);
        notifyItemInserted(mValues.size()-1);
    }

    public void removeLabel(Label label) {
        int indexOf = mValues.indexOf(label);
        mValues.remove(indexOf);
        notifyItemRemoved(indexOf);
    }

    @Override
    public AddLabelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final AddLabelViewHolder holder = AddLabelViewHolder.inflate(parent);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Label label = getEntry(holder.getAdapterPosition());
                mListener.onLabelLongClicked(label);
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final AddLabelViewHolder holder, int position) {
        holder.bind(getEntry(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private Label getEntry(int position) {
        return mValues.get(position);
    }

    public boolean containsLabel(Label label) {
        return mValues.contains(label);
    }

    @Nullable
    public String getCommaSeperatedStringOfLabels() {
        if (mValues.isEmpty()) {
            return null;
        }
        String labels = "";
        for (Label label : mValues) {
            labels = labels + label.getName() + ",";
        }
        //Remove last ","
        labels = labels.substring(0, labels.length()-1);
        return labels;
    }

    public interface Listener {
        void onLabelLongClicked(Label label);
    }
}
