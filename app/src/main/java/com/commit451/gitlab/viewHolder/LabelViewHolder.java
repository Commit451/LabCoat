package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Label;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Label
 */
public class LabelViewHolder extends RecyclerView.ViewHolder {

    public static LabelViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_label, parent, false);
        return new LabelViewHolder(view);
    }

    @BindView(R.id.title) public TextView title;
    @BindView(R.id.color) public View colorView;

    public LabelViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Label label) {
        title.setText(label.getName());
        colorView.setBackgroundColor(label.getColor());
    }
}