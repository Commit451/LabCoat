package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.bypasspicassoimagegetter.BypassPicassoImageGetter;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.BypassImageGetterFactory;
import com.commit451.gitlab.util.DateUtil;
import com.commit451.gitlab.util.ImageUtil;
import com.commit451.gitlab.util.InternalLinkMovementMethod;
import com.vdurmont.emoji.EmojiParser;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;

/**
 * Header for an issue
 */
public class IssueHeaderViewHolder extends RecyclerView.ViewHolder {

    public static IssueHeaderViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_issue, parent, false);
        return new IssueHeaderViewHolder(view);
    }

    @BindView(R.id.description)
    TextView textDescription;
    @BindView(R.id.author_image)
    ImageView imageAuthor;
    @BindView(R.id.author)
    TextView textAuthor;
    @BindView(R.id.milestone_root)
    ViewGroup rootMilestone;
    @BindView(R.id.milestone_text)
    TextView textMilestone;

    private final Bypass bypass;

    public IssueHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        bypass = new Bypass(view.getContext());
    }

    public void bind(Issue issue, Project project) {

        if (TextUtils.isEmpty(issue.getDescription())) {
            textDescription.setVisibility(View.GONE);
        } else {
            textDescription.setVisibility(View.VISIBLE);
            BypassPicassoImageGetter getter = BypassImageGetterFactory.create(textDescription,
                    App.get().getPicasso(),
                    App.get().getAccount().getServerUrl().toString(),
                    project);
            String description = issue.getDescription();
            description = EmojiParser.parseToUnicode(description);
            textDescription.setText(bypass.markdownToSpannable(description, getter));
            textDescription.setMovementMethod(new InternalLinkMovementMethod(App.get().getAccount().getServerUrl()));
        }

        App.get().getPicasso()
                .load(ImageUtil.getAvatarUrl(issue.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .transform(new CircleTransformation())
                .into(imageAuthor);

        String author = "";
        if (issue.getAuthor() != null) {
            author = issue.getAuthor().getName() + " ";
        }
        author += itemView.getResources().getString(R.string.created_issue);
        if (issue.getCreatedAt() != null) {
            author = author + " " + DateUtil.getRelativeTimeSpanString(itemView.getContext(), issue.getCreatedAt());
        }
        textAuthor.setText(author);
        if (issue.getMilestone() != null) {
            rootMilestone.setVisibility(View.VISIBLE);
            textMilestone.setText(issue.getMilestone().getTitle());
        } else {
            rootMilestone.setVisibility(View.GONE);
        }
    }
}