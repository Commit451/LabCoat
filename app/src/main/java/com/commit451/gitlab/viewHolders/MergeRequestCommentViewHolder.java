package com.commit451.gitlab.viewHolders;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.MergeRequestComment;
import com.commit451.gitlab.tools.ImageUtil;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;

/**
 * Notes, aka comments
 * Created by Jawn on 8/6/2015.
 */
public class MergeRequestCommentViewHolder extends RecyclerView.ViewHolder{

    public static MergeRequestCommentViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new MergeRequestCommentViewHolder(view);
    }

    @Bind(R.id.title) public TextView title;
    @Bind(R.id.summary) public TextView summary;
    @Bind(R.id.icon) public ImageView icon;

    public MergeRequestCommentViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(MergeRequestComment comment) {
        if(comment.getAuthor() != null) {
            title.setText(comment.getAuthor().getUsername());
        }

        String temp = "";
        if(comment.getComment() != null) {
            temp = comment.getComment();
        }
        Bypass bypass = new Bypass(itemView.getContext());
        summary.setText(bypass.markdownToSpannable(temp));
        summary.setMovementMethod(LinkMovementMethod.getInstance());

        Uri imageUrl = ImageUtil.getAvatarUrl(comment.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size));
        Picasso.with(itemView.getContext()).load(imageUrl).into(icon);
    }
}