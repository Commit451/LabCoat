package com.commit451.gitlab.util;

import java.util.Arrays;

public class ObjectHelper {
    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static int hash(Object... values) {
        return Arrays.hashCode(values);
    }
}
