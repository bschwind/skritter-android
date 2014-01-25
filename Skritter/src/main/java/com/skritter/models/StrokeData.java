package com.skritter.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StrokeData extends SkritterObject {

    private String rune;
    private String language;
    private Stroke[][] strokes;

    public StrokeData() {
        super();
    }

    public StrokeData(JSONObject jsonObject) {
        super(jsonObject);

        if (jsonObject != null) {
            try {
                List<Stroke[]> allStrokes = new ArrayList<Stroke[]>();
                setRune(jsonObject.getString("rune"));
                setLanguage(jsonObject.getString("lang"));

                JSONArray jsonStrokeArray = jsonObject.getJSONArray("strokes");
                
                // Loop through all the variations you can write
                // Example: 口 - You can write this one in two different ways
                for (int j = 0; j < jsonStrokeArray.length(); j++) {
                    JSONArray strokeOrderVariationJSON = jsonStrokeArray.getJSONArray(j);

                    Stroke[] newStrokeVariation = new Stroke[strokeOrderVariationJSON.length()];

                    for (int i = 0; i < newStrokeVariation.length; i++) {
                        Stroke stroke = new Stroke();
                        JSONArray numberArray = strokeOrderVariationJSON.getJSONArray(i);

                        stroke.strokeID = numberArray.getInt(0);
                        stroke.x = (float)numberArray.getDouble(1);
                        stroke.y = (float)numberArray.getDouble(2);
                        stroke.width = (float)numberArray.getDouble(3);
                        stroke.height = (float)numberArray.getDouble(4);
                        stroke.rotation = (float)numberArray.getDouble(5);

                        newStrokeVariation[i] = stroke;
                    }
                    
                    allStrokes.add(newStrokeVariation);
                }
                
                Stroke[][] newStrokes = new Stroke[allStrokes.size()][];
                
                for (int i = 0; i < allStrokes.size(); i++) {
                    newStrokes[i] = allStrokes.get(i);
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

    public Stroke[][] getStrokes() {
        return strokes;
    }

    public void setStrokes(Stroke[][] strokes) {
        this.strokes = strokes;
    }
}
