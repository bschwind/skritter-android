package com.skritter.models;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginStatus extends SkritterObject {

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

    @Override
    public void loadFromJSONObject(JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                setUserID(jsonObject.getString("user_id"));
                setSecondsBeforeExpiring(jsonObject.getInt("expires_in"));
                setRefreshToken(jsonObject.getString("refresh_token"));
                setAccessToken(jsonObject.getString("access_token"));
                setLoggedIn(true);
            } catch (JSONException e) {
                e.printStackTrace();
                setLoggedIn(false);
            }
        }
    }
}