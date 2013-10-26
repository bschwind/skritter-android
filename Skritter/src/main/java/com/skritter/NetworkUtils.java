package com.skritter;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by bschwind on 10/20/13.
 */
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
