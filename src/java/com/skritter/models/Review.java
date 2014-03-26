package com.skritter.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Review extends SkritterObject {
    private String itemID;
    private int score;
    private boolean bearTime;
    private long submitTime;
    private float reviewTime;
    private float thinkingTime;
    private long currentInterval;
    private long actualInterval;
    private long newInterval;
    private String wordGroup;
    private long previousInterval;
    private boolean previousSuccess;

    public Review() {
        super();
    }

    public Review(JSONObject jsonObject) {
        super(jsonObject);

        if (jsonObject != null) {
            try {
                setItemID(jsonObject.getString("itemID"));
                setScore(jsonObject.getInt("score"));
                setBearTime(jsonObject.getBoolean("bearTime"));
                setSubmitTime(jsonObject.getLong("submitTime"));
                setReviewTime((float)jsonObject.getDouble("reviewTime"));
                setThinkingTime((float)jsonObject.getDouble("thinkingTime"));
                setCurrentInterval(jsonObject.getLong("currentInterval"));
                setActualInterval(jsonObject.getLong("actualInterval"));
                setNewInterval(jsonObject.getLong("newInterval"));
                setWordGroup(jsonObject.getString("wordGroup"));
                setPreviousInterval(jsonObject.getLong("previousInterval"));
                setPreviousSuccess(jsonObject.getBoolean("previousSuccess"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isBearTime() {
        return bearTime;
    }

    public void setBearTime(boolean bearTime) {
        this.bearTime = bearTime;
    }

    public long getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(long submitTime) {
        this.submitTime = submitTime;
    }

    public float getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(float reviewTime) {
        this.reviewTime = reviewTime;
    }

    public float getThinkingTime() {
        return thinkingTime;
    }

    public void setThinkingTime(float thinkingTime) {
        this.thinkingTime = thinkingTime;
    }

    public long getCurrentInterval() {
        return currentInterval;
    }

    public void setCurrentInterval(long currentInterval) {
        this.currentInterval = currentInterval;
    }

    public long getActualInterval() {
        return actualInterval;
    }

    public void setActualInterval(long actualInterval) {
        this.actualInterval = actualInterval;
    }

    public long getNewInterval() {
        return newInterval;
    }

    public void setNewInterval(long newInterval) {
        this.newInterval = newInterval;
    }

    public String getWordGroup() {
        return wordGroup;
    }

    public void setWordGroup(String wordGroup) {
        this.wordGroup = wordGroup;
    }

    public long getPreviousInterval() {
        return previousInterval;
    }

    public void setPreviousInterval(long previousInterval) {
        this.previousInterval = previousInterval;
    }

    public boolean isPreviousSuccess() {
        return previousSuccess;
    }

    public void setPreviousSuccess(boolean previousSuccess) {
        this.previousSuccess = previousSuccess;
    }
}
