package com.commit451.gitlab.api.converter

import com.bluelinelabs.logansquare.typeconverters.DateTypeConverter
import com.commit451.gitlab.model.api.Milestone
import java.text.DateFormat

/**
 * Converts due dates
 */
class DueDateTypeConverter : DateTypeConverter() {

    override fun getDateFormat(): DateFormat {
        return Milestone.DUE_DATE_FORMAT
    }
}