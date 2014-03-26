package com.skritter.math;

import android.graphics.Matrix;

import java.util.Arrays;

public class MathUtil {
    public static float lerp(float a, float b, float t) {
        return a + (b-a) * t;
    }

    public static float median(float[] numbers) {

        float[] temp = numbers.clone();
        Arrays.sort(temp);

        if (temp.length % 2 == 0) {
            int index = temp.length / 2;
            return (temp[index -1] + temp[index]) * 0.5f;
        } else {
            return temp[(numbers.length + 1) / 2];
        }
    }

    public static float easeInOutCubic(float t, float b, float c, float d) {
        if ((t/=d/2) < 1) return c/2*t*t*t + b;
        return c/2*((t-=2)*t*t + 2) + b;
    }
    
    public static Vector2[] transformVectorArray(Matrix matrix, Vector2[] points) {
        float[] sourceArray = new float[points.length * 2];
        
        for (int i = 0; i < points.length; i++) {
            sourceArray[i * 2] = points[i].x;
            sourceArray[(i*2) + 1] = points[i].y;
        }
        
        matrix.mapPoints(sourceArray);
        
        Vector2[] newPoints = new Vector2[points.length];
        
        for (int i = 0; i < newPoints.length; i++) {
            Vector2 newVec2 = new Vector2(sourceArray[i * 2], sourceArray[(i*2) + 1]);
            newPoints[i] = newVec2;
        }
        
        return newPoints;
    }
    
    public static float angleOfPoints(Vector2[] points) {
        if (points.length <= 1) {
            return 0.0f;
        }
        
        Vector2 start = points[0];
        Vector2 end = points[points.length-1];

        Vector2 dir = new Vector2(end.x - start.x, end.y - start.y);

        if (Vector2.length(dir) < 0.0001f) {
            return 0.0f;
        } else {
            return Vector2.angleBetweenVectors(new Vector2(1.0f, 0.0f), dir);
        }
    }
    
    public static float lengthOfPoints(Vector2[] points) {
        float length = 0.0f;
        
        for (int i = 0; i < points.length-1; i++) {
            length += Vector2.length(points[i+1].x - points[i].x, points[i+1].y - points[i].y);
        }
        
        return length;
    }
    
    public static float toRadians(float degrees) {
        return (float)(degrees * (Math.PI / 180.0f));
    }
    
    public static float toDegrees(float radians) {
        return (float)(radians * (180.0f / Math.PI));
    }
}
