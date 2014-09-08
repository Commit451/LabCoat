package com.bd.gitlab.views;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bd.gitlab.R;
import com.bd.gitlab.model.Diff;

public class DiffView extends LinearLayout {
	
	private static final int HEADER_BG = Color.rgb(223, 223, 223);
	private static final int LINE_BG = Color.rgb(238, 238, 238);
	private static final int LINE_BG_ADDED = Color.rgb(204, 255, 204);
	private static final int LINE_BG_REMOVED = Color.rgb(255, 204, 204);
	private static final int CONTENT_BG = Color.rgb(255, 255, 255);
	private static final int CONTENT_BG_ADDED = Color.rgb(204, 255, 221);
	private static final int CONTENT_BG_REMOVED = Color.rgb(255, 221, 221);
	private static final int CONTENT_BG_COMMENT = Color.rgb(250, 250, 250);
	private static final int CONTENT_FONT_COMMENT = Color.rgb(216, 206, 206);
	
	private static final int MARGIN_VIEW_H = 0;
	private static final int MARGIN_VIEW_V = 10;
	private static final int PADDING_HEADER_H = 0;
	private static final int PADDING_HEADER_V = 5;
	private static final int PADDING_CONTENT_H = 5;
	private static final int PADDING_CONTENT_V = 0;
	
	private Diff diff;
	
	private WrappedHorizontalScrollView scrollView;

	/*
	 * Superclass constructors
	 */
	
	public DiffView(Context context) {
		super(context);
	}
	
	public DiffView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DiffView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/*
	 * Custom constructor
	 */
	
	public DiffView(Context context, Diff diff) {
		this(context);
		
		this.diff = diff;
		
		setOrientation(VERTICAL);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(MARGIN_VIEW_H, MARGIN_VIEW_V, MARGIN_VIEW_H, MARGIN_VIEW_V);
		setLayoutParams(params);
		
		addView(generateHeader());

		scrollView = new WrappedHorizontalScrollView(getContext());
		scrollView.setFillViewport(true);
		LinearLayout lineView = new LinearLayout(getContext());
		lineView.setOrientation(VERTICAL);
		scrollView.addView(lineView);
		addView(scrollView);
		
		ArrayList<Diff.Line> lines = (ArrayList<Diff.Line>) diff.getLines();
		for(Diff.Line line : lines)
			lineView.addView(generateRow(line));
		
		setBackgroundResource(R.drawable.border);
		setPadding(1, 1, 1, 1);
	}
	
	private LinearLayout generateHeader() {
		LinearLayout header = new LinearLayout(getContext());
		header.setOrientation(HORIZONTAL);
		header.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		header.setBackgroundColor(HEADER_BG);
		
		ImageView icon = new ImageView(getContext());
		if(diff.isNewFile())
			icon.setImageResource(R.drawable.ic_added);
		else if(diff.isDeletedFile())
			icon.setImageResource(R.drawable.ic_removed);
		else
			icon.setImageResource(R.drawable.ic_changed);
		icon.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		icon.setPadding(10, 10, 10, 10);
		header.addView(icon);
		
		TextView title = new TextView(getContext());
		title.setText(diff.getNewPath());
		title.setTypeface(title.getTypeface(), Typeface.BOLD);
		title.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		title.setPadding(PADDING_HEADER_H, PADDING_HEADER_V, PADDING_HEADER_H, PADDING_HEADER_V);
		header.addView(title);
		
		return header;
	}
	
	private LinearLayout generateRow(Diff.Line line) {
		if(line == null)
            return null;

        LinearLayout row = new LinearLayout(getContext());
		row.setOrientation(HORIZONTAL);
		row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		TextView oldLine = new TextView(getContext());
		oldLine.setText(line.oldLine);
		oldLine.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		oldLine.setEms(2);
		oldLine.setGravity(Gravity.CENTER_HORIZONTAL);
		row.addView(oldLine);
		
		TextView newLine = new TextView(getContext());
		newLine.setText(line.newLine);
		newLine.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		newLine.setEms(2);
		newLine.setGravity(Gravity.CENTER_HORIZONTAL);
		row.addView(newLine);
		
		TextView content = new TextView(getContext());
		content.setText(line.lineContent);
		content.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		content.setPadding(PADDING_CONTENT_H, PADDING_CONTENT_V, PADDING_CONTENT_H, PADDING_CONTENT_V);
		row.addView(content);
		
		if(line.lineType == null) //lineContent probably equals "\ No newline at end of file"
			return row;
		
		switch(line.lineType) {
			case NORMAL:
				oldLine.setBackgroundColor(LINE_BG);
				newLine.setBackgroundColor(LINE_BG);
				content.setBackgroundColor(CONTENT_BG);
				break;
			case ADDED:
				oldLine.setBackgroundColor(LINE_BG_ADDED);
				newLine.setBackgroundColor(LINE_BG_ADDED);
				content.setBackgroundColor(CONTENT_BG_ADDED);
				break;
			case REMOVED:
				oldLine.setBackgroundColor(LINE_BG_REMOVED);
				newLine.setBackgroundColor(LINE_BG_REMOVED);
				content.setBackgroundColor(CONTENT_BG_REMOVED);
				break;
			case COMMENT:
				oldLine.setBackgroundColor(LINE_BG);
				newLine.setBackgroundColor(LINE_BG);
				content.setBackgroundColor(CONTENT_BG_COMMENT);
				content.setTextColor(CONTENT_FONT_COMMENT);
				content.setGravity(Gravity.CENTER_HORIZONTAL);
				break;
		}
		
		return row;
	}

	public void setWrapped(boolean wrapped) {
		scrollView.setWrapped(wrapped);
		scrollView.invalidate();
		scrollView.requestLayout();
	}
}
