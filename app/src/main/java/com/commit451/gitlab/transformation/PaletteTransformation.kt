package com.commit451.gitlab.transformation

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette
import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation
import java.lang.ref.WeakReference
import java.util.*

/**
 * Applies palette and stuff
 */
class PaletteTransformation private constructor() : Transformation {

    companion object {
        private val INSTANCE = PaletteTransformation()
        private val CACHE = WeakHashMap<Bitmap, androidx.palette.graphics.Palette>()

        fun getPalette(bitmap: Bitmap): androidx.palette.graphics.Palette? {
            return CACHE[bitmap]
        }

        /**
         * Obtains a [PaletteTransformation] to extract [Palette] information.
         * @return A [PaletteTransformation]
         */
        fun instance(): PaletteTransformation {
            return INSTANCE
        }
    }

    /**
     * A [Target] that receives [Palette] information in its callback.
     * @see Target
     */
    abstract class PaletteTarget : Target {
        /**
         * Callback when an image has been successfully loaded.
         * Note: You must not recycle the bitmap.
         * @param palette The extracted [Palette]
         */
        protected abstract fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom, palette: androidx.palette.graphics.Palette?)

        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            val palette = getPalette(bitmap)
            onBitmapLoaded(bitmap, from, palette)
        }
    }

    /**
     * A [Callback] that receives [Palette] information in its callback.
     * @see Callback
     */
    abstract class PaletteCallback(imageView: ImageView) : Callback {
        private val imageView: WeakReference<ImageView>

        init {
            this.imageView = WeakReference(imageView)
        }

        protected abstract fun onSuccess(palette: androidx.palette.graphics.Palette?)

        override fun onSuccess() {
            if (getImageView() == null) {
                return
            }
            val bitmap = (getImageView()!!.drawable as BitmapDrawable).bitmap // Ew!
            val palette = getPalette(bitmap)
            onSuccess(palette)
        }

        private fun getImageView(): ImageView? {
            return imageView.get()
        }
    }

    //# Transformation Contract
    override fun transform(source: Bitmap): Bitmap {

        val palette = androidx.palette.graphics.Palette.from(source).generate()
        CACHE.put(source, palette)
        return source
    }

    override fun key(): String {
        return "" // Stable key for all requests. An unfortunate requirement.
    }
}
