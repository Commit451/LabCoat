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

    @BindView(R.id.title)
    public TextView textTitle;
    @BindView(R.id.file_name)
    public TextView textFileName;

    public SnippetViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Snippet snippet) {
        textTitle.setText(snippet.getTitle());
        if (snippet.getFileName() != null) {
            textFileName.setVisibility(View.VISIBLE);
            textFileName.setText(snippet.getFileName());
        } else {
            textFileName.setVisibility(View.GONE);
        }
    }
}