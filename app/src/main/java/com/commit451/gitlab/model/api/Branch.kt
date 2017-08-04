package com.commit451.gitlab.model.api

import com.commit451.gitlab.util.ObjectUtil
import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
class Branch {

    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "protected")
    var isProtected: Boolean = false

    override fun equals(o: Any?): Boolean {
        if (o !is Branch) {
            return false
        }

        val branch = o as Branch?
        return ObjectUtil.equals(name, branch!!.name)
    }

    override fun hashCode(): Int {
        return ObjectUtil.hash(name!!)
    }
}
