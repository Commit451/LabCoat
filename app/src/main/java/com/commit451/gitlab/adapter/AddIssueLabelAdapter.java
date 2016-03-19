package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
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

    public AddIssueLabelAdapter() {
        mValues = new ArrayList<>();
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

    @Override
    public AddLabelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return AddLabelViewHolder.inflate(parent);
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
}
