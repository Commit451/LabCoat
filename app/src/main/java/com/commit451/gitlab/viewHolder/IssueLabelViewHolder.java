package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Shows the labels for an issue
 */
public class IssueLabelViewHolder extends RecyclerView.ViewHolder {

    public static IssueLabelViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_issue_label, parent, false);
        return new IssueLabelViewHolder(view);
    }

    @BindView(R.id.title)
    TextView mTitleView;

    public IssueLabelViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(String label) {
        mTitleView.setText(label);
    }
}