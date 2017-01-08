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

    private ArrayList<Label> values;
    private Listener listener;

    public AddIssueLabelAdapter(Listener listener) {
        values = new ArrayList<>();
        this.listener = listener;
    }

    public void setLabels(Collection<Label> labels) {
        values.clear();
        addLabels(labels);
    }

    public void addLabels(Collection<Label> labels) {
        if (labels != null) {
            values.addAll(labels);
        }
        notifyDataSetChanged();
    }

    public void addLabel(Label label) {
        values.add(label);
        notifyItemInserted(values.size()-1);
    }

    public void removeLabel(Label label) {
        int indexOf = values.indexOf(label);
        values.remove(indexOf);
        notifyItemRemoved(indexOf);
    }

    @Override
    public AddLabelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final AddLabelViewHolder holder = AddLabelViewHolder.inflate(parent);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Label label = getEntry(holder.getAdapterPosition());
                listener.onLabelLongClicked(label);
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
        return values.size();
    }

    private Label getEntry(int position) {
        return values.get(position);
    }

    public boolean containsLabel(Label label) {
        return values.contains(label);
    }

    @Nullable
    public String getCommaSeperatedStringOfLabels() {
        if (values.isEmpty()) {
            return null;
        }
        String labels = "";
        for (Label label : values) {
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
