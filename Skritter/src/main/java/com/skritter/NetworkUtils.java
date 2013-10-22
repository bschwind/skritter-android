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
    public static JSONObject getJsonObjectFromHTTPResponse(HttpResponse response) {
        BufferedReader br;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String line = br.readLine();

            while (line != null) {
                sb.append(line).append("\n");
                line = br.readLine();
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(sb.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return jsonObject;
    }
}
