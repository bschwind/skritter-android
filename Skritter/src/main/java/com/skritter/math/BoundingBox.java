package com.skritter.math;

public class BoundingBox {
    public float x, y;
    public float width, height;

    public BoundingBox(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static BoundingBox getBounds(Vector2[] points, int numPoints) {
        if (points.length == 0 || numPoints == 0) {
            return new BoundingBox(0, 0, 0, 0);
        }

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < numPoints; i++) {
            Vector2 p = points[i];

            if (p.x < minX) {
                minX = p.x;
            }
            if (p.y < minY) {
                minY = p.y;
            }
            if (p.x > maxX) {
                maxX = p.x;
            }
            if (p.y > maxY) {
                maxY = p.y;
            }
        }

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }
}
