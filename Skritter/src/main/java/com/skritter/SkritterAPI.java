package com.skritter;

import android.net.http.AndroidHttpClient;
import android.util.Base64;

import com.skritter.models.LoginStatus;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class SkritterAPI {
    private static String apiClientID = "bschwindapiclient";
    private static String apiClientSecret = "c7e7251c01b830b8ed87ea4bb39fdd";

    public static LoginStatus login(String username, String password) {
        LoginStatus loginStatus = new LoginStatus();

        if ("".equals(username) || "".equals(password)) {
            loginStatus.setLoggedIn(false);
            return loginStatus;
        }

        String url = "http://www.skritter.com/api/v0/oauth2/token";

        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("androidSkritter");
        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("grant_type", "password"));
        nameValuePair.add(new BasicNameValuePair("client_id", apiClientID));
        nameValuePair.add(new BasicNameValuePair("username", username));
        nameValuePair.add(new BasicNameValuePair("password", password));

        String credentials = apiClientID + ":" + apiClientSecret;
        credentials = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
        credentials = "basic " + credentials.trim();

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            loginStatus.setLoggedIn(false);
            return loginStatus;
        }

        httpPost.addHeader("AUTHORIZATION", credentials);
        String responseBody;

        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            responseBody = httpClient.execute(httpPost, responseHandler);
        } catch (ClientProtocolException cpe) {
            cpe.printStackTrace();
            loginStatus.setLoggedIn(false);
            return loginStatus;
        } catch (IOException io) {
            io.printStackTrace();
            loginStatus.setLoggedIn(false);
            return loginStatus;
        } finally {
            httpClient.close();
        }

        JSONObject jsonObject = NetworkUtils.getJsonObjectFromHTTPResponseBody(responseBody);

        String statusCode = "";

        if (jsonObject != null) {
            statusCode = jsonObject.optString("statusCode");
        }

        if ("200".equals(statusCode)) {
            loginStatus = new LoginStatus(jsonObject);
            loginStatus.setLoggedIn(true);
            return loginStatus;
        } else {
            loginStatus.setLoggedIn(false);
            return loginStatus;
        }
    }

    public static JSONObject fetchRecentItems(String accessToken) {
        return fetchStudyItemsWithCursor(accessToken, null);
    }

    public static JSONObject fetchAllItems(String accessToken) {
        // This is extremely slow. I don't even know if it ever finishes
        // Use the batching system instead

        JSONObject jsonObject = fetchStudyItemsWithCursor(accessToken, null);

        if (jsonObject == null) {
            return null;
        }

        String cursor = jsonObject.optString("cursor");

        while (cursor != null) {
            JSONObject newJsonObject = fetchStudyItemsWithCursor(accessToken, cursor);
            try {
                jsonObject.accumulate("Items", newJsonObject.optJSONArray("Items"));
                jsonObject.accumulate("Vocabs", newJsonObject.optJSONArray("Vocabs"));
            } catch (JSONException e) {
                e.printStackTrace();
                return jsonObject;
            }

            cursor = newJsonObject.optString("cursor");
        }

        return jsonObject;
    }

    private static JSONObject fetchStudyItemsWithCursor(String accessToken, String cursor) {
        String url = "http://www.skritter.com/api/v0/items?";

        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("sort", "next"));
        nameValuePair.add(new BasicNameValuePair("bearer_token", accessToken));
        nameValuePair.add(new BasicNameValuePair("include_vocabs", "true"));
        nameValuePair.add(new BasicNameValuePair("include_strokes", "true"));
        nameValuePair.add(new BasicNameValuePair("include_decomps", "true"));
        nameValuePair.add(new BasicNameValuePair("gzip", "false"));

        if (cursor != null) {
            nameValuePair.add(new BasicNameValuePair("cursor", cursor));
        }

        String paramString = URLEncodedUtils.format(nameValuePair, "utf-8");
        url += paramString;

        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("androidSkritter");
        HttpGet httpGet = new HttpGet(url);

        String responseBody = "";

        try {
            ResponseHandler<String> responseHandler=new BasicResponseHandler();
            responseBody = httpClient.execute(httpGet, responseHandler);
        } catch (ClientProtocolException cpe) {
            cpe.printStackTrace();
            return null;
        } catch (IOException io) {
            io.printStackTrace();
            return null;
        } finally {
            httpClient.close();
        }

        JSONObject jsonObject = NetworkUtils.getJsonObjectFromHTTPResponseBody(responseBody);

        String statusCode = "";

        if (jsonObject != null) {
            statusCode = jsonObject.optString("statusCode");
        }

        if ("200".equals(statusCode)) {
            return jsonObject;
        } else {
            return null;
        }
    }
}
