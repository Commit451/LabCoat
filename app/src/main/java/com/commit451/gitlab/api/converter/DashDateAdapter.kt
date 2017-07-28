package com.commit451.gitlab.api.converter

import com.commit451.gitlab.model.api.Milestone
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Converts due dates
 */
class DashDateAdapter {

    companion object {
        val format by lazy {
            SimpleDateFormat("yyyy-MM-d", Locale.US)
        }
    }

    @Retention(AnnotationRetention.RUNTIME)
    @JsonQualifier
    annotation class DueDate

    @ToJson
    fun toJson(@DueDate date: Date): String {
        return format.format(date)
    }

    @FromJson
    @DueDate
    fun fromJson(json: String): Date {
        return format.parse(json)
    }
}