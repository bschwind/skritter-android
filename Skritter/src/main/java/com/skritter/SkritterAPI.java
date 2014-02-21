package com.skritter;

import android.net.http.AndroidHttpClient;
import android.util.Base64;

import com.skritter.models.LoginStatus;
import com.skritter.models.Sentence;
import com.skritter.models.StrokeData;
import com.skritter.models.StudyItem;
import com.skritter.models.Vocab;
import com.skritter.persistence.SentenceTable;
import com.skritter.persistence.SkritterDatabaseHelper;
import com.skritter.persistence.StrokeDataTable;
import com.skritter.persistence.StudyItemTable;
import com.skritter.persistence.VocabTable;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class SkritterAPI {
//    private static String apiClientID = "bschwindapiclient";
//    private static String apiClientSecret = "c7e7251c01b830b8ed87ea4bb39fdd";

    private static String apiClientID = "mcfarljwapiclient";
    private static String apiClientSecret = "e3872517fed90a820e441531548b8c";

    public static LoginStatus login(String username, String password) {
        LoginStatus loginStatus = new LoginStatus();

        if ("".equals(username) || "".equals(password)) {
            loginStatus.setLoggedIn(false);
            return loginStatus;
        }

        String url = "http://beta.skritter.com/api/v0/oauth2/token";

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

    public static void batchGetAndStoreStudyItems(String accessToken, SkritterDatabaseHelper db) {
        String url = "http://beta.skritter.com/api/v0/batch?";

        JSONArray requestArray = new JSONArray();
        JSONObject requestJSON = new JSONObject();
        JSONObject params = new JSONObject();
        try {
//            params.put("lang", "ja");
            params.put("sort", "changed"); // todo - This should be "next", but then strokes aren't included
            params.put("offset", 0);
            params.put("include_vocabs", "true");
            params.put("include_strokes", "true");
            params.put("include_sentences", "true");
            params.put("include_heisigs", "true");
            params.put("include_top_mnemonics", "true");
            params.put("include_decomps", "true");


            requestJSON.put("path", "api/v0/items");
            requestJSON.put("method", "GET");
            requestJSON.put("params", params);
            requestJSON.put("spawner", true); // todo - SET THIS TO TRUE, but "Item queries cannot be run in parallel"

            requestArray.put(requestJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<NameValuePair> bearerTokenNameValue = new ArrayList<NameValuePair>();
        bearerTokenNameValue.add(new BasicNameValuePair("bearer_token", accessToken));

        String paramString = URLEncodedUtils.format(bearerTokenNameValue, "utf-8");
        url += paramString;

        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("androidSkritter");

        HttpPost httpPost = new HttpPost(url);

        String credentials = apiClientID + ":" + apiClientSecret;
        credentials = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
        credentials = "basic " + credentials.trim();
        httpPost.addHeader("AUTHORIZATION", credentials);

        try {
            httpPost.setEntity(new StringEntity(requestArray.toString()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        String responseBody = "";

        try {
            ResponseHandler<String> responseHandler=new BasicResponseHandler();
            responseBody = httpClient.execute(httpPost, responseHandler);
        } catch (ClientProtocolException cpe) {
            cpe.printStackTrace();
            return;
        } catch (IOException io) {
            io.printStackTrace();
            return;
        } finally {
            httpClient.close();
        }

        JSONObject jsonObject = NetworkUtils.getJsonObjectFromHTTPResponseBody(responseBody);

        String statusCode = "";

        if (jsonObject != null) {
            statusCode = jsonObject.optString("statusCode");
        }

        if ("200".equals(statusCode)) {
            JSONObject batch = jsonObject.optJSONObject("Batch");
            int batchID = batch.optInt("id");

            while (true) {
                JSONObject jsonBatch = getBatch(accessToken, batchID);

                if (jsonBatch == null) {
                    return;
                }

                int runningBatches = jsonBatch.optJSONObject("Batch").optInt("runningRequests");
                int numRequestsReady = jsonBatch.optJSONObject("Batch").optJSONArray("Requests").length();

                if (runningBatches > 0 && numRequestsReady == 0) {
                    try {
                        Thread.sleep(2000);
                        continue;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < jsonBatch.optJSONObject("Batch").optJSONArray("Requests").length(); i++) {
                    JSONObject response = jsonBatch.optJSONObject("Batch").optJSONArray("Requests").optJSONObject(i).optJSONObject("response");

                    if (response == null) {
                        continue;
                    }

                    populateDBWithJSONItems(db, response);
                }

                if (runningBatches == 0) {
                    break;
                }
            }
        }
    }

    private static void populateDBWithJSONItems(SkritterDatabaseHelper db, JSONObject response) {
        populateItems(db, response);
        populateVocab(db, response);
        populateStrokes(db, response);
        populateSentences(db, response);
    }

    private static void populateItems(SkritterDatabaseHelper db, JSONObject response) {
        // Populate study items from the response
        JSONArray studyItemJSONArray = response.optJSONArray("Items");

        if (studyItemJSONArray == null || studyItemJSONArray.length() == 0) {
            return;
        }

        for (int i = 0; i < studyItemJSONArray.length(); i++) {
            JSONObject studyItemJSONObject = studyItemJSONArray.optJSONObject(i);

            if (studyItemJSONObject == null) {
                continue;
            }

            StudyItem item = new StudyItem(studyItemJSONObject);

            if (item == null) {
                continue;
            }

            StudyItemTable.getInstance().create(db, item);
        }
    }

    private static void populateVocab(SkritterDatabaseHelper db, JSONObject response) {
        // Populate Vocabs which were included in the study item response
        JSONArray vocabJSONArray = response.optJSONArray("Vocabs");

        if (vocabJSONArray == null || vocabJSONArray.length() == 0) {
            return;
        }

        for (int i = 0; i < vocabJSONArray.length(); i++) {
            JSONObject vocabJSONObject = vocabJSONArray.optJSONObject(i);

            if (vocabJSONObject == null) {
                continue;
            }

            Vocab vocab = new Vocab(vocabJSONObject);

            if (vocab == null) {
                continue;
            }

            VocabTable.getInstance().create(db, vocab);
        }
    }

    private static void populateStrokes(SkritterDatabaseHelper db, JSONObject response) {
        // Populate Strokes which were included in the study item response
        JSONArray strokeJSONArray = response.optJSONArray("Strokes");

        if (strokeJSONArray == null || strokeJSONArray.length() == 0) {
            return;
        }

        for (int i = 0; i < strokeJSONArray.length(); i++) {
            JSONObject strokeJSONObject = strokeJSONArray.optJSONObject(i);

            if (strokeJSONObject == null) {
                continue;
            }

            StrokeData strokeData = new StrokeData(strokeJSONObject);

            if (strokeData == null) {
                continue;
            }

            StrokeDataTable.getInstance().create(db, strokeData);
        }
    }
    
    private static void populateSentences(SkritterDatabaseHelper db, JSONObject response) {
        // Populate sentences which were included in the study item response
        JSONArray sentenceJSONArray = response.optJSONArray("Sentences");

        if (sentenceJSONArray == null || sentenceJSONArray.length() == 0) {
            return;
        }

        for (int i = 0; i < sentenceJSONArray.length(); i++) {
            JSONObject sentenceJSONObject = sentenceJSONArray.optJSONObject(i);

            if (sentenceJSONObject == null) {
                continue;
            }

            Sentence sentence = new Sentence(sentenceJSONObject);

            if (sentence == null) {
                continue;
            }

            SentenceTable.getInstance().create(db, sentence);
        }
    }

    private static JSONObject getBatch(String accessToken, int batchID) {
        String url = "http://beta.skritter.com/api/v0/batch/" + batchID + "?";

        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("bearer_token", accessToken));
//        nameValuePair.add(new BasicNameValuePair("request_ids", "" + requestID));

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

    private static JSONObject fetchStudyItemsWithCursor(String accessToken, String cursor) {
        String url = "http://beta.skritter.com/api/v0/items?";

        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("sort", "next"));
        nameValuePair.add(new BasicNameValuePair("bearer_token", accessToken));
        nameValuePair.add(new BasicNameValuePair("include_vocabs", "true"));
        nameValuePair.add(new BasicNameValuePair("include_strokes", "true"));
        nameValuePair.add(new BasicNameValuePair("include_sentences", "true"));
        nameValuePair.add(new BasicNameValuePair("include_heisigs", "true"));
        nameValuePair.add(new BasicNameValuePair("include_top_mnemonics", "true"));
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
