package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.DateUtil;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.BindView;
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

    @BindView(R.id.commit_author_image)
    ImageView image;
    @BindView(R.id.commit_author)
    TextView textAuthor;
    @BindView(R.id.commit_time)
    TextView textTime;
    @BindView(R.id.commit_title)
    TextView textTitle;
    @BindView(R.id.commit_message)
    TextView textMessage;

    public DiffHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(RepositoryCommit commit) {
        App.get().getPicasso()
                .load(ImageUtil.getAvatarUrl(commit.getAuthorEmail(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .transform(new CircleTransformation())
                .into(image);

        textAuthor.setText(commit.getAuthorName());
        if (commit.getCreatedAt() == null) {
            textTime.setText(null);
        } else {
            textTime.setText(DateUtil.getRelativeTimeSpanString(itemView.getContext(), commit.getCreatedAt()));
        }

        textTitle.setText(commit.getTitle());
        String message = extractMessage(commit.getTitle(), commit.getMessage());
        textMessage.setText(message);
        textMessage.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * This extracts the trailing part of the textTitle as it is displayed in the GitLab web interface
     * (the commit message also contains the commit textTitle)
     */
    private String extractMessage(String title, String message) {
        if (!TextUtils.isEmpty(message)) {
            boolean ellipsis = title.endsWith("\u2026") && message.charAt(title.length() - 1) != '\u2026';
            String trailing = message.substring(title.length() - (ellipsis ? 1 : 0));
            return trailing.equals("\u2026") ? "" : ((ellipsis ? "\u2026" : "") + trailing).trim();
        }
        return title;
    }
}
