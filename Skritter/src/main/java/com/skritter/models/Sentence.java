package com.skritter.models;

import com.skritter.utils.JSONUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class Sentence extends SkritterObject {
    private String id;
    private String[] containedVocabIDs;
    private String definitions; // This is a JSON Object: {"en":"(become) thirsty"}
    private String language;
    private boolean isRareKanji;
    private String reading; // The phonetic reading (kana or pinyin) of the word or phrase
    private boolean starred;
    private int toughness;
    private String toughnessString;
    private String writing;

    public Sentence() {
        super();
    }

    public Sentence(JSONObject jsonObject) {
        super(jsonObject);

        if (jsonObject != null) {
            try {
                setId(jsonObject.getString("id"));
                setContainedVocabIDs(JSONUtil.getStringArrayFromJSONArray(jsonObject.optJSONArray("containedVocabIds")));
                setDefinitions(jsonObject.getString("definitions"));
                setLanguage(jsonObject.getString("lang"));
                setRareKanji(jsonObject.getBoolean("rareKanji"));
                setReading(jsonObject.getString("reading"));
                setStarred(jsonObject.getBoolean("starred"));
                setToughness(jsonObject.getInt("toughness"));
                setToughnessString(jsonObject.getString("toughnessString"));                
                setWriting(jsonObject.getString("writing"));              
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

    public String[] getContainedVocabIDs() {
        return containedVocabIDs;
    }

    public void setContainedVocabIDs(String[] containedVocabIDs) {
        this.containedVocabIDs = containedVocabIDs;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isRareKanji() {
        return isRareKanji;
    }

    public void setRareKanji(boolean rareKanji) {
        isRareKanji = rareKanji;
    }

    public int getToughness() {
        return toughness;
    }

    public void setToughness(int toughness) {
        this.toughness = toughness;
    }

    public String getWriting() {
        return writing;
    }

    public void setWriting(String writing) {
        this.writing = writing;
    }

    public String getToughnessString() {
        return toughnessString;
    }

    public void setToughnessString(String toughnessString) {
        this.toughnessString = toughnessString;
    }

    public String getDefinitions() {
        return definitions;
    }

    public String getDefinitionByLanguage(String language) {
        String definitionJSON = getDefinitions();

        if (definitionJSON != null && definitionJSON.length() > 0) {
            JSONObject json;
            try {
                json = new JSONObject(definitionJSON);
            } catch (JSONException e) {
                return "";
            }

            return json.optString(language);
        }

        return "";
    }

    public void setDefinitions(String definitions) {
        this.definitions = definitions;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public String getReading() {
        return reading;
    }

    public void setReading(String reading) {
        this.reading = reading;
    }
}
