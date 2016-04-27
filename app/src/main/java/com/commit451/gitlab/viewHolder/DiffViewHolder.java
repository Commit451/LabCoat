package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alorma.diff.lib.DiffTextView;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Diff;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Displays a diff to a user
 */
public class DiffViewHolder extends RecyclerView.ViewHolder {

    public static DiffViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diff, parent, false);
        return new DiffViewHolder(view);
    }

    @BindView(R.id.file_title) TextView mFileTitle;
    @BindView(R.id.diff) DiffTextView mDiffTextView;

    public DiffViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Diff diff) {
        mFileTitle.setText(diff.getFileName());
        mDiffTextView.setText(diff.getDiff());
    }
}
