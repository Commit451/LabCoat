package com.commit451.gitlab.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

public class WrappedHorizontalScrollView extends HorizontalScrollView {

	protected boolean mIsWrapped = true;

	public WrappedHorizontalScrollView(Context context) {
		super(context);
	}

	public WrappedHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WrappedHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (!isWrapped()) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);

		if(widthMode == MeasureSpec.UNSPECIFIED) {
			return;
		}

		int width = MeasureSpec.getSize(widthMeasureSpec);
		int maxHeight = 0;
		int childMeasuredState = 0;

		if(getChildCount() > 0) {
			final View child = getChildAt(0);
			final LayoutParams lp = (LayoutParams) child.getLayoutParams();

			childMeasuredState = child.getMeasuredState();
			int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
			int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, lp.height);

			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			maxHeight = child.getMeasuredHeight();
		}

		setMeasuredDimension(width, resolveSizeAndState(maxHeight, heightMeasureSpec, childMeasuredState << MEASURED_HEIGHT_STATE_SHIFT));
	}

	public boolean isWrapped() {
		return mIsWrapped;
	}

	public void setWrapped(boolean wrapped) {
		mIsWrapped = wrapped;
	}
}
