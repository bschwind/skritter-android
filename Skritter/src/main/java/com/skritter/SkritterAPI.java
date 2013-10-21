package com.skritter;

import android.net.http.AndroidHttpClient;
import android.util.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bschwind on 10/20/13.
 */
public class SkritterAPI {
    private static String apiClientID = "bschwindapiclient";
    private static String apiClientSecret = "c7e7251c01b830b8ed87ea4bb39fdd";

    public static boolean login(String username, String password) {
        if ("".equals(username) || "".equals(password)) {
            return false;
        }

        String url = "https://www.skritter.com/api/v0/oauth2/token";

        HttpClient httpClient = AndroidHttpClient.newInstance("androidSkritter");
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
            return false;
        }

        httpPost.addHeader("AUTHORIZATION", credentials);
        HttpResponse response;

        try {
            response = httpClient.execute(httpPost);
        } catch (ClientProtocolException cpe) {
            cpe.printStackTrace();
            return false;
        } catch (IOException io) {
            io.printStackTrace();
            return false;
        }

        String userID;
        int secondsBeforeExpiring;
        String refreshToken;
        String accessToken;

        JSONObject jsonObject = NetworkUtils.getJsonObjectFromHTTPResponse(response);

        if (jsonObject != null) {
            try {
                userID = jsonObject.getString("user_id");
                secondsBeforeExpiring = jsonObject.getInt("expires_in");
                refreshToken = jsonObject.getString("refresh_token");
                accessToken = jsonObject.getString("access_token");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if ("OK".equals(response.getStatusLine().getReasonPhrase())) {
            return true;
        } else {
            return false;
        }
    }
}
