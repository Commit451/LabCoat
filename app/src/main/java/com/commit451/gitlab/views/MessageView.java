package com.commit451.gitlab.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.commit451.gitlab.model.Commit;

import java.util.ArrayList;
import java.util.List;

public class MessageView extends LinearLayout {

    private WrappedHorizontalScrollView scrollView;

    public MessageView(Context context) {
        super(context);
    }

    public MessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageView(Context context, Commit commit) {
        this(context);

        scrollView = new WrappedHorizontalScrollView(getContext());
        scrollView.setFillViewport(true);
        LinearLayout innerView = new LinearLayout(getContext());
        scrollView.addView(innerView);
        addView(scrollView);

        innerView.setOrientation(LinearLayout.VERTICAL);

        List<Commit.Line> lines = commit.getLines();
        if (lines != null) {
            for (Commit.Line line : lines) {
                innerView.addView(generateRow(line));
            }
        }
    }

    private LinearLayout generateRow(Commit.Line line) {
        if(line == null)
            return null;

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView content = new TextView(getContext());
        content.setText(line.lineContent);
        row.addView(content);

        return row;
    }

    public void setWrapped(boolean wrapped) {
        scrollView.setWrapped(wrapped);
        scrollView.invalidate();
        scrollView.requestLayout();
    }
}
