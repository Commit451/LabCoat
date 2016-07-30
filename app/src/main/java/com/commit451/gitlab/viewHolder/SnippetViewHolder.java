package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Snippet;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Snippet
 */
public class SnippetViewHolder extends RecyclerView.ViewHolder {

    public static SnippetViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_snippet, parent, false);
        return new SnippetViewHolder(view);
    }

    @BindView(R.id.title) public TextView title;
    @BindView(R.id.file_name) public TextView fileName;

    public SnippetViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Snippet snippet) {
        title.setText(snippet.getTitle());
        if (snippet.getFileName() != null) {
            fileName.setVisibility(View.VISIBLE);
            fileName.setText(snippet.getFileName());
        } else {
            fileName.setVisibility(View.GONE);
        }
    }
}