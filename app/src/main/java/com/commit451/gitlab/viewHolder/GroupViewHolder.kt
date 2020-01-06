package com.commit451.gitlab.viewHolder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.model.api.VISIBILITY_PUBLIC
import com.github.ivbaranov.mli.MaterialLetterIcon

/**
 * View associated with a group
 */
class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): GroupViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_group, parent, false)
            return GroupViewHolder(view)
        }
    }

    val image: ImageView by bindView(R.id.image)
    private val iconLetter: MaterialLetterIcon by bindView(R.id.letter)
    private val textName: TextView by bindView(R.id.name)

    fun bind(group: Group, @ColorInt color: Int) {
        textName.text = group.name

        if (group.avatarUrl.isNullOrBlank() || group.visibility != VISIBILITY_PUBLIC) {
            image.visibility = View.GONE
            iconLetter.visibility = View.VISIBLE
            iconLetter.letter = group.name!!.substring(0, 1)
            iconLetter.letterColor = Color.WHITE
            iconLetter.shapeColor = color
        } else {
            iconLetter.visibility = View.GONE
            image.visibility = View.VISIBLE
            image.load(group.avatarUrl)
        }
    }
}
