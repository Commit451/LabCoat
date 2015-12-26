package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.util.DateUtils;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;

/**
 * Adapter for merge request detail
 * Created by John on 11/16/15.
 */
public class MergeRequestHeaderViewHolder extends RecyclerView.ViewHolder {

    public static MergeRequestHeaderViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_merge_request, parent, false);
        return new MergeRequestHeaderViewHolder(view);
    }

    @Bind(R.id.description)
    TextView description;
    @Bind(R.id.author_image)
    ImageView authorImage;
    @Bind(R.id.author) TextView author;
    Bypass mBypass;

    public MergeRequestHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        mBypass = new Bypass(view.getContext());
    }

    public void bind(MergeRequest mergeRequest) {
        if (TextUtils.isEmpty(mergeRequest.getDescription())) {
            description.setVisibility(View.GONE);
        } else {
            description.setVisibility(View.VISIBLE);
            description.setText(mBypass.markdownToSpannable(mergeRequest.getDescription()));
            description.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (mergeRequest.getAuthor() != null) {
            GitLabClient.getPicasso()
                    .load(ImageUtil.getAvatarUrl(mergeRequest.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                    .into(authorImage);
            author.setText(mergeRequest.getAuthor().getName() + " "
                    + itemView.getResources().getString(R.string.created_issue) + " "
                    + DateUtils.getRelativeTimeSpanString(itemView.getContext(), mergeRequest.getCreatedAt()));
        }
        if (mergeRequest.getCreatedAt() != null) {
            DateUtils.getRelativeTimeSpanString(itemView.getContext(), mergeRequest.getCreatedAt());
        }
    }
}
