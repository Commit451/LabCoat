package com.bd.gitlab.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;
import com.bd.gitlab.R;

public class FilterEditText extends EditText {

    int actionX, actionY;

    private DrawableClickListener clickListener;

    public FilterEditText (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        if(text.length() > 0)
            super.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_input_delete, 0);
        else
            super.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Rect bounds;
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            actionX = (int) event.getX();
            actionY = (int) event.getY();

            if(getText().length() > 0) {
                bounds = this.getCompoundDrawables()[2].getBounds();

                int x, y;
                int extraTapArea = 13;

                x = (int) (actionX + extraTapArea);
                y = (int) (actionY - extraTapArea);
                x = getWidth() - x;

                if(x <= 0)
                    x += extraTapArea;
                if(y <= 0)
                    y = actionY;

                if(bounds.contains(x, y) && clickListener != null) {
                    playSoundEffect(android.view.SoundEffectConstants.CLICK);
                    clickListener.onClick(DrawableClickListener.DrawablePosition.RIGHT);
                    event.setAction(MotionEvent.ACTION_CANCEL);

                    return false;
                }

                return super.onTouchEvent(event);
            }

        }
        return super.onTouchEvent(event);
    }

    public void setDrawableClickListener(DrawableClickListener listener) {
        this.clickListener = listener;
    }
}
