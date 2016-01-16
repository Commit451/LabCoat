package com.commit451.gitlab.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import in.uncod.android.bypass.Bypass;
import timber.log.Timber;

/**
 * Original credits: http://stackoverflow.com/a/25530488/504611
 */
public class PicassoImageGetter implements Bypass.ImageGetter {

    private final Picasso mPicasso;
    private final TextView mTextView;
    private int maxWidth = -1;

    public PicassoImageGetter(final TextView textView, final Picasso picasso) {
        mTextView = textView;
        mPicasso = picasso;
    }

    @Override
    public Drawable getDrawable(String source) {

        final BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder();

        final String finalSource = source;
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(final Void... meh) {
                try {
                    return mPicasso.load(finalSource).get();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                try {
                    if (maxWidth == -1) {
                        int horizontalPadding = mTextView.getPaddingLeft() + mTextView.getPaddingRight();
                        maxWidth = mTextView.getMeasuredWidth() - horizontalPadding;
                        if (maxWidth == 0) {
                            maxWidth = Integer.MAX_VALUE;
                        }
                    }

                    final BitmapDrawable drawable = new BitmapDrawable(mTextView.getResources(), bitmap);
                    final double aspectRatio = 1.0 * drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
                    final int width = Math.min(maxWidth, drawable.getIntrinsicWidth());
                    final int height = (int) (width / aspectRatio);

                    drawable.setBounds(0, 0, width, height);

                    result.setDrawable(drawable);
                    result.setBounds(0, 0, width, height);

                    mTextView.setText(mTextView.getText()); // invalidate() doesn't work correctly...
                } catch (Exception e) {
                    Timber.e(e, null);
                }
            }

        }.execute((Void) null);

        return result;
    }

    private static class BitmapDrawablePlaceHolder extends BitmapDrawable {

        protected Drawable drawable;

        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

    }

}