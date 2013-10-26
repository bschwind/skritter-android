package com.skritter.models;

import org.json.JSONObject;

/**
 * Created by bschwind on 10/26/13.
 */
public abstract class SkritterObject {
    public abstract void loadFromJSONObject(JSONObject jsonObject);
}
