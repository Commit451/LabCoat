package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Label;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows the label on a screen where you can add labels
 */
public class AddLabelViewHolder extends RecyclerView.ViewHolder {

    public static AddLabelViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_add_label, parent, false);
        return new AddLabelViewHolder(view);
    }

    @Bind(R.id.title)
    TextView mTitleView;

    public AddLabelViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Label label) {
        mTitleView.setText(label.getName());
        mTitleView.setBackgroundColor(label.getColor());
    }
}