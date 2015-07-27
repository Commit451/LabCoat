package com.commit451.gitlab.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.EditText;

import com.commit451.gitlab.R;

public class FilterEditText extends EditText {

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
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            if(getText().length() > 0 && this.getCompoundDrawables()[2] != null) {
                int actionX = (int) event.getRawX();
                int drawableX = getRight();

                drawableX -= getCompoundDrawables()[2].getBounds().width();
                drawableX -= ((ViewGroup.MarginLayoutParams) getLayoutParams()).rightMargin;
                drawableX -= 15;

                if(actionX >= drawableX && clickListener != null) {
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
