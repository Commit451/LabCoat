package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.easel.Easel;
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

    @BindView(R.id.title)
    public TextView title;

    int colorHighlighted;

    public TagViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        colorHighlighted = Easel.getThemeAttrColor(itemView.getContext(), R.attr.colorControlHighlight);
    }

    public void bind(Tag tag, boolean selected) {
        title.setText(tag.getName());
        if (selected) {
            itemView.setBackgroundColor(colorHighlighted);
        } else {
            itemView.setBackground(null);
        }
    }
}