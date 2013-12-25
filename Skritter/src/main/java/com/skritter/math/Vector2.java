package com.skritter.math;

public class Vector2 {
    public float x;
    public float y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static float distance(Vector2 v1, Vector2 v2) {
        float x = v2.x - v1.x;
        float y = v2.y - v1.y;
        return (float)Math.sqrt((x * x) + (y * y));
    }
}
