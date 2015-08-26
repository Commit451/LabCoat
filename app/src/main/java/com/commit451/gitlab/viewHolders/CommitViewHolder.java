package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.DiffLine;
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

    @Bind(R.id.commit_image) ImageView image;
    @Bind(R.id.commit_message) TextView message;
    @Bind(R.id.commit_author) TextView author;
    @Bind(R.id.commit_time) TextView time;

    public CommitViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(DiffLine commit) {
        String url = Gravatar.init()
                .with(commit.getAuthorEmail())
                .size(itemView.getResources().getDimensionPixelSize(R.dimen.image_size))
                .build();

        Picasso.with(itemView.getContext())
                .load(url)
                .into(image);

        message.setText(commit.getTitle());
        author.setText(commit.getAuthorName());
        time.setText(DateUtils.getRelativeTimeSpanString(commit.getCreatedAt().getTime()));
    }
}
