/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.commit451.gitlab.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View
import com.commit451.gitlab.R

class DividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }

    private val divider: Drawable
    private val dividerHeight: Int = context.resources.getDimensionPixelSize(R.dimen.divider_height)

    private var showFirstDivider = true
    private val showLastDivider = false

    init {
        val a = context.obtainStyledAttributes(ATTRS)
        divider = a.getDrawable(0)
        a.recycle()
    }

    constructor(context: Context, first: Boolean) : this(context) {
        showFirstDivider = first
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        drawVertical(c, parent)
    }

    fun drawVertical(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0..childCount - 1) {
            val child = parent.getChildAt(i)
            if (isSkipped(child, parent)) continue
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + dividerHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State?) {
        if (isSkipped(view, parent)) return
        outRect.set(0, 0, 0, dividerHeight)
    }

    private fun isSkipped(child: View, parent: RecyclerView): Boolean {
        val position = parent.getChildAdapterPosition(child)
        // Skip the first divider if needed
        if (!showFirstDivider && position == 0) return true
        // Skip the last divider if needed
        if (!showLastDivider && position == parent.adapter.itemCount - 1) return true
        // Otherwise don't skip
        return false
    }
}