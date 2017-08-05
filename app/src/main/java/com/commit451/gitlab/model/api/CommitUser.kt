package com.commit451.gitlab.model.api

import com.commit451.gitlab.util.ObjectUtil
import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel(Parcel.Serialization.BEAN)
open class CommitUser {
    @field:Json(name = "id")
    var id: String? = null
    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "username")
    var username: String? = null
    @field:Json(name = "state")
    var state: String? = null
    @field:Json(name = "avatar_url")
    var avatarUrl: String? = null
    @field:Json(name = "web_url")
    var webUrl: String? = null

    override fun equals(o: Any?): Boolean {
        if (o !is CommitUser) {
            return false
        }

        val commit = o as CommitUser?
        return ObjectUtil.equals(id, commit!!.id)
    }

    override fun hashCode(): Int {
        return ObjectUtil.hash(id!!)
    }
}
