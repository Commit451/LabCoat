package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Tag;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Label
 */
public class TagViewHolder extends RecyclerView.ViewHolder {

    public static TagViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag, parent, false);
        return new TagViewHolder(view);
    }

    @BindView(R.id.title) public TextView title;

    public TagViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Tag tag) {
        title.setText(tag.getName());
    }
}