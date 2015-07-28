package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.DiffLine;
import com.commit451.gitlab.tools.Repository;
import com.commit451.gitlab.views.CompoundTextView;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.tkeunebr.gravatar.Gravatar;

/**
 * Files, yay!
 * Created by Jawn on 6/11/2015.
 */
public class CommitViewHolder extends RecyclerView.ViewHolder {

    public static CommitViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_commit, parent, false);
        return new CommitViewHolder(view);
    }

    @Bind(R.id.title) TextView title;
    @Bind(R.id.summary) CompoundTextView summary;
    @Bind(R.id.custom) TextView custom;

    public CommitViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(DiffLine commit) {
        title.setText(commit.getTitle());
        summary.setText(commit.getAuthorName());
        custom.setText(DateUtils.getRelativeTimeSpanString(commit.getCreatedAt().getTime()));

        float percent = Repository.displayWidth / 720f;
        int size = (int) (40f * percent);

        String url = Gravatar.init().with(commit.getAuthorEmail()).size(size).build();
        Picasso.with(itemView.getContext()).load(url).into(summary);
    }
}
