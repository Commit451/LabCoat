package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Represents a merge request within a list
 */
public class MergeRequestViewHolder extends RecyclerView.ViewHolder {

    public static MergeRequestViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_merge_request, parent, false);
        return new MergeRequestViewHolder(view);
    }

    @BindView(R.id.request_image)
    ImageView image;
    @BindView(R.id.request_title)
    TextView textTitle;
    @BindView(R.id.request_author)
    TextView textAuthor;

    public MergeRequestViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(MergeRequest item) {
        App.get().getPicasso()
                .load(ImageUtil.getAvatarUrl(item.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .transform(new CircleTransformation())
                .into(image);

        if (item.getAuthor() != null) {
            textAuthor.setText(item.getAuthor().getUsername());
        } else {
            textAuthor.setText("");
        }

        if (item.getTitle() != null) {
            textTitle.setText(item.getTitle());
        } else {
            textTitle.setText("");
        }
    }
}
