package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
class Member : UserBasic() {

    companion object {

        fun getAccessLevel(accessLevel: String): Int {
            when (accessLevel.toLowerCase()) {
                "guest" -> return 10
                "reporter" -> return 20
                "developer" -> return 30
                "master" -> return 40
                "owner" -> return 50
            }

            throw IllegalStateException("No known code for this access level")
        }

        fun getAccessLevel(accessLevel: Int): String {
            when (accessLevel) {
                10 -> return "Guest"
                20 -> return "Reporter"
                30 -> return "Developer"
                40 -> return "Master"
                50 -> return "Owner"
            }

            return "Unknown"
        }
    }

    @Json(name = "access_level")
    var accessLevel: Int = 0
}
