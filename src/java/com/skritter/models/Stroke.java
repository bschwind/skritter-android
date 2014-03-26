package com.skritter.models;

public class Stroke {
    public int strokeID;
    public float x, y;
    public float width, height;
    public float rotation;

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Stroke)) {
            return false;
        }
        
        Stroke otherStroke = (Stroke)other;
        
        return this.strokeID == otherStroke.strokeID
                && this.x == otherStroke.x
                && this.y == otherStroke.y
                && this.width == otherStroke.width
                && this.height == otherStroke.height
                && this.rotation == rotation;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return strokeID + "-" + x + "-" + y + "-" + width + "-" + height + "-" + rotation;
    }
}
