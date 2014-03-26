package com.skritter.math;

public class Vector2 {
    public float x;
    public float y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public Vector2(Vector2 copy) {
        this.x = copy.x;
        this.y = copy.y;
    }

    public static float distance(Vector2 v1, Vector2 v2) {
        float x = v2.x - v1.x;
        float y = v2.y - v1.y;
        return (float)Math.sqrt((x * x) + (y * y));
    }
    
    public static float dot(Vector2 v1, Vector2 v2) {
        return v1.x * v2.x + v1.y * v1.y;
    }
    
    public static float length(Vector2 v) {
        return (float)Math.sqrt(v.x * v.x + v.y * v.y);
    }
    
    public static float length(float x, float y) {
        return (float)Math.sqrt(x * x + y * y);
    }
    
    public static float angleBetweenVectors(Vector2 v1, Vector2 v2) {
        float angle =  (float)(Math.acos(dot(v1, v2) / (length(v1) * length(v2))));
        
        if(v1.x * v2.y - v1.y * v2.x < 0) {
            angle = -angle;
        }
        
        return angle;
    }
}
