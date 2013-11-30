package com.skritter.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StrokeData extends SkritterObject {

    private String rune;
    private String language;
    private Stroke[] strokes;

    public StrokeData() {
        super();
    }

    public StrokeData(JSONObject jsonObject) {
        super(jsonObject);

        if (jsonObject != null) {
            try {
                setRune(jsonObject.getString("rune"));
                setLanguage(jsonObject.getString("lang"));

                JSONArray jsonStrokeArray = jsonObject.getJSONArray("strokes").getJSONArray(0);
                Stroke[] newStrokes = new Stroke[jsonStrokeArray.length()];

                for (int i = 0; i < newStrokes.length; i++) {
                    Stroke stroke = new Stroke();
                    JSONArray numberArray = jsonStrokeArray.getJSONArray(i);

                    stroke.strokeID = numberArray.getInt(0);
                    stroke.x = (float)numberArray.getDouble(1);
                    stroke.y = (float)numberArray.getDouble(2);
                    stroke.width = (float)numberArray.getDouble(3);
                    stroke.height = (float)numberArray.getDouble(4);
                    stroke.rotation = (float)numberArray.getDouble(5);

                    newStrokes[i] = stroke;
                }

                setStrokes(newStrokes);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getRune() {
        return rune;
    }

    public void setRune(String rune) {
        this.rune = rune;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Stroke[] getStrokes() {
        return strokes;
    }

    public void setStrokes(Stroke[] strokes) {
        this.strokes = strokes;
    }
}
