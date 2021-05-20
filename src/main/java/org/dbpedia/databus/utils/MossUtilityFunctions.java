package org.dbpedia.databus.utils;

public final class MossUtilityFunctions {


    public static String getValFromArray(String[] str_array) {
        if (str_array == null) {
            return "";
        } else {
            return str_array[0];
        }
    }
}
