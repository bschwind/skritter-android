package com.skritter;

import android.app.Application;

public class SkritterApplication extends Application {
    public static final String SKRITTER_SHARED_PREFERENCES = "SkritterPreferences";

    public static final class PreferenceKeys {
        public static final String USER_ID = "userID";
        public static final String SECONDS_BEFORE_EXPIRING = "secondsBeforeExpiring";
        public static final String REFRESH_TOKEN = "refreshToken";
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String HIDE_READING = "hideReadingPreferenceKey";
        public static final String HIDE_DEFINITION = "hideDefinitionPreferenceKey";
        public static final String SHOW_USER_CORNERS = "showUserCornersPreferenceKey";
        public static final String SHOW_PARAM_CORNERS = "showParamCornersPreferenceKey";
    }

}
