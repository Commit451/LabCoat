package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.util.DateUtil
import java.util.*

/**
 * Builds, woot
 */
class BuildViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): BuildViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_build, parent, false)
            return BuildViewHolder(view)
        }
    }

    @BindView(R.id.name)
    lateinit var textBuildName: TextView
    @BindView(R.id.number)
    lateinit var textBuildNumber: TextView
    @BindView(R.id.status)
    lateinit var textStatus: TextView
    @BindView(R.id.duration)
    lateinit var textDuration: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(build: Build) {
        val buildNameText = String.format(itemView.resources.getString(R.string.build_name), build.name)
        textBuildName.text = buildNameText

        val buildNumberText = String.format(itemView.resources.getString(R.string.build_number), build.id)
        textBuildNumber.text = buildNumberText

        val statusText = String.format(itemView.resources.getString(R.string.build_status), build.status)
        textStatus.text = statusText
        var finishedTime: Date? = build.finishedAt
        if (finishedTime == null) {
            finishedTime = Date()
        }
        var startedAt: Date? = build.startedAt
        if (startedAt == null) {
            startedAt = Date()
        }
        val timeTaken = DateUtil.getTimeTaken(startedAt, finishedTime)
        val durationStr = String.format(itemView.resources.getString(R.string.build_duration), timeTaken)
        textDuration.text = durationStr
    }
}
