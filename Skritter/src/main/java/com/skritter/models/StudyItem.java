package com.skritter.models;

import com.skritter.utils.JSONUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class StudyItem extends SkritterObject {
    private String id;
    private String part; // can be rdng,rune,tone, or defn
    private String[] vocabIDs;
    private String style; // both, trad, simp
    private long timeStudied;
    private long next;
    private long last;
    private long interval;
    private String[] vocabListIDs;
    private String[] sectionIDs;
    private int reviews;
    private int successes;
    private long created;
    private long changed;
    private boolean previousSuccess;
    private long previousInterval;

    public StudyItem() {
        super();
    }

    public StudyItem(JSONObject jsonObject) {
        super(jsonObject);

        if (jsonObject != null) {
            try {
                setId(jsonObject.getString("id"));
                setPart(jsonObject.getString("part"));
                setVocabIDs(JSONUtil.getStringArrayFromJSONArray(jsonObject.getJSONArray("vocabIds")));
                setStyle(jsonObject.getString("style"));
                setTimeStudied(jsonObject.getLong("timeStudied"));
                setNext(jsonObject.getLong("next"));
                setLast(jsonObject.optLong("last"));
                setInterval(jsonObject.optLong("interval"));
                setVocabListIDs(JSONUtil.getStringArrayFromJSONArray(jsonObject.getJSONArray("vocabListIds")));
                setSectionIDs(JSONUtil.getStringArrayFromJSONArray(jsonObject.getJSONArray("sectionIds")));
                setReviews(jsonObject.getInt("reviews"));
                setSuccesses(jsonObject.getInt("successes"));
                setCreated(jsonObject.getLong("created"));
                setChanged(jsonObject.getLong("changed"));
                setPreviousSuccess(jsonObject.optBoolean("previousSuccess"));
                setPreviousInterval(jsonObject.optLong("previousInterval"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String[] getVocabIDs() {
        return vocabIDs;
    }

    public void setVocabIDs(String[] vocabIDs) {
        this.vocabIDs = vocabIDs;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public long getTimeStudied() {
        return timeStudied;
    }

    public void setTimeStudied(long timeStudied) {
        this.timeStudied = timeStudied;
    }

    public long getNext() {
        return next;
    }

    public void setNext(long next) {
        this.next = next;
    }

    public long getLast() {
        return last;
    }

    public void setLast(long last) {
        this.last = last;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public String[] getVocabListIDs() {
        return vocabListIDs;
    }

    public void setVocabListIDs(String[] vocabListIDs) {
        this.vocabListIDs = vocabListIDs;
    }

    public String[] getSectionIDs() {
        return sectionIDs;
    }

    public void setSectionIDs(String[] sectionIDs) {
        this.sectionIDs = sectionIDs;
    }

    public int getReviews() {
        return reviews;
    }

    public void setReviews(int reviews) {
        this.reviews = reviews;
    }

    public int getSuccesses() {
        return successes;
    }

    public void setSuccesses(int successes) {
        this.successes = successes;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getChanged() {
        return changed;
    }

    public void setChanged(long changed) {
        this.changed = changed;
    }

    public boolean isPreviousSuccess() {
        return previousSuccess;
    }

    public void setPreviousSuccess(boolean previousSuccess) {
        this.previousSuccess = previousSuccess;
    }

    public long getPreviousInterval() {
        return previousInterval;
    }

    public void setPreviousInterval(long previousInterval) {
        this.previousInterval = previousInterval;
    }

    public boolean isRune() {
        return "rune".equals(getPart());
    }

    public boolean isReading() {
        return "rdng".equals(getPart());
    }

    public boolean isDefinition() {
        return "defn".equals(getPart());
    }

    public boolean isTone() {
        return "tone".equals(getPart());
    }

    public String toString() {
        return id;
    }
}
