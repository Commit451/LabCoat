package com.commit451.gitlab.extension

import com.commit451.gitlab.model.api.Diff

val Diff.fileName: String
    get() {
        if (newPath!!.contains("/")) {
            val paths = newPath!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return paths[paths.size - 1]
        } else {
            return newPath!!
        }
    }