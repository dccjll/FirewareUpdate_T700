package com.dsm.t700.update;

/**
 * Created by dessmann on 16/6/30.
 */
public class Util {
    public static boolean isNotEmpty(String str) {
        return (str != null) && (!"null".equalsIgnoreCase(str)) && (str.length() > 0);
    }

    public static boolean isEmpty(String str) {
        return (str == null) || ("null".equalsIgnoreCase(str)) || (str.length() == 0);
    }
}
