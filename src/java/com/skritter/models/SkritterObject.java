package com.skritter.models;

import org.json.JSONObject;

public abstract class SkritterObject {
    private long oid;

    public SkritterObject() {

    }

    public SkritterObject(JSONObject jsonObject) {

    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }
}
