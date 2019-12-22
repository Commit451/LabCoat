package com.commit451.gitlab.image

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.palette.graphics.Palette
import coil.target.ImageViewTarget

class PaletteImageViewTarget(
        imageView: ImageView,
        private val block: (palette: Palette) -> Unit
) : ImageViewTarget(imageView) {

    override fun onSuccess(result: Drawable) {
        super.onSuccess(result)
        if (result is BitmapDrawable) {
            Palette.from(result.bitmap).generate {
                it?.let { block.invoke(it) }
            }
        }
    }
}
