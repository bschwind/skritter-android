package com.skritter;

import android.net.http.AndroidHttpClient;
import android.util.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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

    public static class LoginStatus {
        private boolean loggedIn;
        private String userID;
        private int secondsBeforeExpiring;
        private String refreshToken;
        private String accessToken;

        public boolean isLoggedIn() {
            return loggedIn;
        }

        public void setLoggedIn(boolean loggedIn) {
            this.loggedIn = loggedIn;
        }

        public String getUserID() {
            return userID;
        }

        public void setUserID(String userID) {
            this.userID = userID;
        }

        public int getSecondsBeforeExpiring() {
            return secondsBeforeExpiring;
        }

        public void setSecondsBeforeExpiring(int secondsBeforeExpiring) {
            this.secondsBeforeExpiring = secondsBeforeExpiring;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    public static LoginStatus login(String username, String password) {
        LoginStatus loginStatus = new LoginStatus();

        if ("".equals(username) || "".equals(password)) {
            loginStatus.setLoggedIn(false);
            return loginStatus;
        }

        String url = "https://www.skritter.com/api/v0/oauth2/token";

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
        HttpResponse response;

        try {
            response = httpClient.execute(httpPost);
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

        JSONObject jsonObject = NetworkUtils.getJsonObjectFromHTTPResponse(response);

        if (jsonObject != null) {
            try {
                loginStatus.setUserID(jsonObject.getString("user_id"));
                loginStatus.setSecondsBeforeExpiring(jsonObject.getInt("expires_in"));
                loginStatus.setRefreshToken(jsonObject.getString("refresh_token"));
                loginStatus.setAccessToken(jsonObject.getString("access_token"));
            } catch (JSONException e) {
                e.printStackTrace();
                loginStatus.setLoggedIn(false);
                return loginStatus;
            }
        }

        if ("OK".equals(response.getStatusLine().getReasonPhrase())) {
            loginStatus.setLoggedIn(true);
            return loginStatus;
        } else {
            loginStatus.setLoggedIn(false);
            return loginStatus;
        }
    }
}
