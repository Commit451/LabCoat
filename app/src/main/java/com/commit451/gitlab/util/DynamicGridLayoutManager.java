package com.commit451.gitlab.util;

import android.content.Context;
import android.support.annotation.DimenRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * {@link GridLayoutManager} which dynamically sizes its number of columns based on
 * {@link #setMinimumWidth(int)}
 */
public class DynamicGridLayoutManager extends GridLayoutManager {

    private int mMinWidth = 100;
    private Context mContext;
    private int mNumColumns = -1;

    public DynamicGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public DynamicGridLayoutManager(Context context) {
        super(context, 2);
        mContext = context;
    }

    public DynamicGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
        mContext = context;
    }

    public void setMinimumWidth(int minimumWidth) {
        mMinWidth = minimumWidth;
        if (getWidth() != 0) {
            int columns = getWidth() / mMinWidth;
            setSpanCount(Math.max(1, columns));
        }
    }

    public void setMinimumWidthDimension(@DimenRes int dimension) {
        setMinimumWidth(mContext.getResources().getDimensionPixelSize(dimension));
    }


    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        int width = View.MeasureSpec.getSize(widthSpec);
        mNumColumns = Math.max(1, width / mMinWidth);
        setSpanCount(mNumColumns);
        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    /**
     * Get the number of computed columns. Useful for using with
     * {@link SpanSizeLookup}
     * @return the number of columns or -1 if the columns have not yet been measured.
     */
    public int getNumColumns() {
        return mNumColumns;
    }
}
