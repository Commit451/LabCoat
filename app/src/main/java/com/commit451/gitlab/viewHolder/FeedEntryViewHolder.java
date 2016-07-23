package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.rss.Entry;
import com.commit451.gitlab.transformation.CircleTransformation;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Represents the view of an item in the RSS feed
 */
public class FeedEntryViewHolder extends RecyclerView.ViewHolder {

    public static FeedEntryViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entry, parent, false);
        return new FeedEntryViewHolder(view);
    }

    @BindView(R.id.image) ImageView mImageView;
    @BindView(R.id.title) TextView mTitleView;
    @BindView(R.id.description) TextView mSummaryView;

    public FeedEntryViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Entry entry) {
        App.instance().getPicasso()
                .load(entry.getThumbnail().getUrl())
                .transform(new CircleTransformation())
                .into(mImageView);

        mTitleView.setText(Html.fromHtml(entry.getTitle()));
        mSummaryView.setText(Html.fromHtml(entry.getSummary()));
    }
}
