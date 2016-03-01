package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.DateUtils;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Header that gives the details of a merge request
 */
public class DiffHeaderViewHolder extends RecyclerView.ViewHolder {

    public static DiffHeaderViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_diff, parent, false);
        return new DiffHeaderViewHolder(view);
    }

    @Bind(R.id.commit_author_image) ImageView mImageView;
    @Bind(R.id.commit_author) TextView mAuthorView;
    @Bind(R.id.commit_time) TextView mTimeView;
    @Bind(R.id.commit_title) TextView mTitleView;
    @Bind(R.id.commit_message) TextView mMessageView;

    public DiffHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(RepositoryCommit commit) {
        GitLabClient.getPicasso()
                .load(ImageUtil.getAvatarUrl(commit.getAuthorEmail(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .transform(new CircleTransformation())
                .into(mImageView);

        mAuthorView.setText(commit.getAuthorName());
        mTimeView.setText(DateUtils.getRelativeTimeSpanString(itemView.getContext(), commit.getCreatedAt()));
        mTitleView.setText(commit.getTitle());
        String message = extractMessage(commit.getTitle(), commit.getMessage());
        mMessageView.setText(message);
        mMessageView.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * This extracts the trailing part of the title as it is displayed in the GitLab web interface
     * (the commit message also contains the commit title)
     */
    private String extractMessage(String title, String message) {
        boolean ellipsis = title.endsWith("\u2026") && message.charAt(title.length() - 1) != '\u2026';
        String trailing = message.substring(title.length() - (ellipsis ? 1 : 0));
        return trailing.equals("\u2026") ? "" : ((ellipsis ? "\u2026" : "") + trailing).trim();
    }
}
