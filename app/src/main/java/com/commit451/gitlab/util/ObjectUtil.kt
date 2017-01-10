package com.commit451.gitlab.util

import java.util.*

object ObjectUtil {

    fun equals(a: Any?, b: Any?): Boolean {
        return if (a == null) b == null else a == b
    }

    fun hash(vararg values: Any): Int {
        return Arrays.hashCode(values)
    }
}
