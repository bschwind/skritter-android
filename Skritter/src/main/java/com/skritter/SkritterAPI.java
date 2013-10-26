package com.skritter;

import android.net.http.AndroidHttpClient;
import android.util.Base64;

import com.skritter.models.LoginStatus;
import com.skritter.models.StudyItem;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
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
        String responseBody;

        try {
            ResponseHandler<String> responseHandler=new BasicResponseHandler();
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
            loginStatus.loadFromJSONObject(jsonObject);
            loginStatus.setLoggedIn(true);
            return loginStatus;
        } else {
            loginStatus.setLoggedIn(false);
            return loginStatus;
        }
    }

    public static void fetchItems(String accessToken) {

        List<StudyItem> studyItems = new ArrayList<StudyItem>();
        String cursor = fetchStudyItemsAndAppendToList(studyItems, accessToken, null);

        while (cursor != null) {
            cursor = fetchStudyItemsAndAppendToList(studyItems, accessToken, cursor);
        }

        System.out.println(studyItems);
    }

    private static String fetchStudyItemsAndAppendToList(List<StudyItem> studyItems, String accessToken, String cursor) {
        String url = "http://www.skritter.com/api/v0/items?";

        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("sort", "changed"));
        nameValuePair.add(new BasicNameValuePair("parts", "rune"));
        nameValuePair.add(new BasicNameValuePair("bearer_token", accessToken));
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
            JSONArray studyItemJSONArray = jsonObject.optJSONArray("Items");

            for (int i = 0; i < studyItemJSONArray.length(); i++) {
                JSONObject studyItemJSONObject = studyItemJSONArray.optJSONObject(i);
                StudyItem item = new StudyItem();
                item.loadFromJSONObject(studyItemJSONObject);

                studyItems.add(item);
            }

            if ("".equals(jsonObject.optString("cursor"))) {
                return null;
            }

            return jsonObject.optString("cursor");
        } else {
            return null;
        }
    }
}
