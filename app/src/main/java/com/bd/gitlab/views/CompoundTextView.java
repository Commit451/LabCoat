package com.bd.gitlab.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class CompoundTextView extends TextView implements Target {

    public CompoundTextView(Context context) {
        super(context);
    }

    public CompoundTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompoundTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        setImage(new BitmapDrawable(this.getResources(), bitmap));
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        setImage(errorDrawable);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        setImage(placeHolderDrawable);
    }

    private void setImage(Drawable drawable) {
        this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }
}
