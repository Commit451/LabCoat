package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.bypasspicassoimagegetter.BypassPicassoImageGetter;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Note;
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
 * Notes, aka comments
 */
public class NoteViewHolder extends RecyclerView.ViewHolder {

    public static NoteViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @BindView(R.id.title)
    TextView textTitle;
    @BindView(R.id.summary)
    TextView textSummary;
    @BindView(R.id.creation_date)
    TextView textCreationDate;
    @BindView(R.id.icon)
    ImageView imageAvatar;

    public NoteViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Note note, Bypass bypass, Project project) {
        if (note.getCreatedAt() != null) {
            textCreationDate.setText(DateUtil.getRelativeTimeSpanString(itemView.getContext(), note.getCreatedAt()));
        }

        if (note.getAuthor() != null) {
            textTitle.setText(note.getAuthor().getUsername());
        }

        String summary = "";
        if (note.getBody() != null) {
            summary = note.getBody();
            summary = EmojiParser.parseToUnicode(summary);
        }

        BypassPicassoImageGetter getter = BypassImageGetterFactory.create(textSummary,
                App.get().getPicasso(),
                App.get().getAccount().getServerUrl().toString(),
                project);
        textSummary.setText(bypass.markdownToSpannable(summary, getter));
        textSummary.setMovementMethod(new InternalLinkMovementMethod(App.get().getAccount().getServerUrl()));

        App.get().getPicasso()
                .load(ImageUtil.getAvatarUrl(note.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .transform(new CircleTransformation())
                .into(imageAvatar);
    }
}
