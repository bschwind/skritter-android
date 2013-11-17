package com.skritter.utils;

import org.json.JSONArray;

public class JSONUtil {
    public static String[] getStringArrayFromJSONArray(JSONArray jsonArray) {
        if (jsonArray == null) {
            return new String[0];
        }

        String[] strings = new String[jsonArray.length()];

        for (int i = 0; i < strings.length; i++) {
            strings[i] = jsonArray.optString(i);
        }

        return strings;
    }
}
