package com.skritter;

import org.json.JSONException;
import org.json.JSONObject;

public class NetworkUtils {
    public static JSONObject getJsonObjectFromHTTPResponseBody(String responseBody) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(responseBody);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return jsonObject;
    }
}
