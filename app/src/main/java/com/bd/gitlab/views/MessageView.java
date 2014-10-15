package com.bd.gitlab.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bd.gitlab.model.DiffLine;

import java.util.ArrayList;

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

	public MessageView(Context context, DiffLine diffLine) {
		this(context);

		scrollView = new WrappedHorizontalScrollView(getContext());
		scrollView.setFillViewport(true);
		LinearLayout innerView = new LinearLayout(getContext());
		scrollView.addView(innerView);
		addView(scrollView);

		innerView.setOrientation(LinearLayout.VERTICAL);

		ArrayList<DiffLine.Line> lines = (ArrayList<DiffLine.Line>) diffLine.getLines();
		for(DiffLine.Line line : lines)
			innerView.addView(generateRow(line));
	}

	private LinearLayout generateRow(DiffLine.Line line) {
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
