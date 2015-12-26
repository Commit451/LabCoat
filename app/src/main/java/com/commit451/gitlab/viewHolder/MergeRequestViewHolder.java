package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Represents a merge request within a list
 * Created by Jawn on 9/20/2015.
 */
public class MergeRequestViewHolder extends RecyclerView.ViewHolder {

    public static MergeRequestViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_merge_request, parent, false);
        return new MergeRequestViewHolder(view);
    }

    @Bind(R.id.request_image) ImageView image;
    @Bind(R.id.request_title) TextView title;
    @Bind(R.id.request_author) TextView author;

    public MergeRequestViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(MergeRequest item) {
        GitLabClient.getPicasso()
                .load(ImageUtil.getAvatarUrl(item.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .into(image);
        if (item.getAuthor() != null && item.getAuthor().getUsername() != null) {
            author.setText(item.getAuthor().getUsername());
        } else {
            author.setText("");
        }
        if (item.getTitle() != null) {
            title.setText(item.getTitle());
        } else {
            title.setText("");
        }
    }
}
