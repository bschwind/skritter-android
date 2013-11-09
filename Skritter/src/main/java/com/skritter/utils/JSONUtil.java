package com.skritter.utils;

import org.json.JSONArray;

public class JSONUtil {
    public static String[] getStringArrayFromJSONArray(JSONArray jsonArray) {
        String[] strings = new String[jsonArray.length()];

        for (int i = 0; i < strings.length; i++) {
            strings[i] = jsonArray.optString(i);
        }

        return strings;
    }
}
