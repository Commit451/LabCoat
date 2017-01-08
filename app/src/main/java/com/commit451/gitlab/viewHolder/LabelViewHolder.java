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

    @BindView(R.id.title)
    public TextView textTitle;
    @BindView(R.id.color)
    public View viewColor;

    public LabelViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Label label) {
        textTitle.setText(label.getName());
        viewColor.setBackgroundColor(label.getColor());
    }
}