package com.skritter.utils;

import com.skritter.models.Vector2;

import java.util.Arrays;

public class MathUtil {
    public static float lerp(float a, float b, float t) {
        return a + (b-a) * t;
    }

    public static float distance(Vector2 v1, Vector2 v2) {
        float x = v2.x - v1.x;
        float y = v2.y - v1.y;
        return (float)Math.sqrt((x * x) + (y * y));
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
}
