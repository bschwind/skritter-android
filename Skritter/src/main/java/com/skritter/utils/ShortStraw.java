package com.skritter.utils;

import com.skritter.math.BoundingBox;
import com.skritter.math.MathUtil;
import com.skritter.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class ShortStraw {
    private static Vector2[] resampledPoints = new Vector2[1024];

    private static final float diagonalInterval = 40.0f;
    private static final int strawWindow = 3;
    private static final float medianThreshold = 0.95f;
    private static final float lineThreshold = 0.95f;

    private static float determineResampleSpacing(Vector2[] points, int numPoints) {
        BoundingBox box = BoundingBox.getBounds(points, numPoints);
        float diagonalDistance = Vector2.distance(new Vector2(box.x, box.y), new Vector2(box.x + box.width, box.y + box.height));

        return diagonalDistance / diagonalInterval;
    }

    private static int getResampledPoints(Vector2[] points, int numPoints, float s) {
        if (points.length == 0 || numPoints == 0) {
            return 0;
        }

        List<Vector2> pointsList = new ArrayList<Vector2>(numPoints);
        for (int i = 0; i < numPoints; i++) {
            pointsList.add(points[i]);
        }

        float distance = 0.0f;
        List<Vector2> resampledList = new ArrayList<Vector2>();
        resampledList.add(points[0]);

        for (int i = 1; i < pointsList.size(); i++) {
            Vector2 p1 = pointsList.get(i-1);
            Vector2 p2 = pointsList.get(i);
            float currentDist = Vector2.distance(p1, p2);
            if (distance + currentDist >= s) {
                float qx = p1.x + ((s - distance) / currentDist) * (p2.x - p1.x);
                float qy = p1.y + ((s - distance) / currentDist) * (p2.y - p1.y);
                Vector2 q = new Vector2(qx, qy);
                resampledList.add(q);
                pointsList.add(i, q);
                distance = 0;
            } else {
                distance += currentDist;
            }
        }

        resampledList.add(points[numPoints-1]);

        for (int i = 0; i < resampledList.size(); i++) {
            //todo - IndexOutOfBoundsException is thrown here for longer lines
            resampledPoints[i] = resampledList.get(i);
        }

        return resampledList.size();
    }

    private static int[] getCorners(Vector2[] points, int numPoints) {
        List<Integer> indices = new ArrayList<Integer>();
        indices.add(0);
        float[] straws = new float[numPoints];

        for (int i = strawWindow; i < numPoints - strawWindow; i++) {
            straws[i] = Vector2.distance(points[i-strawWindow], points[i+strawWindow]);
        }

        float t = MathUtil.median(straws) * medianThreshold;

        for (int i = strawWindow; i < numPoints - strawWindow; i++) {
            if (straws[i] < t) {
                float localMin = Float.POSITIVE_INFINITY;
                int localMinIndex = i;
                while (i < straws.length && straws[i] < t) {
                    if (straws[i] < localMin) {
                        localMin = straws[i];
                        localMinIndex = i;
                    }
                    i++;
                }

                indices.add(localMinIndex);
            }
        }

        indices.add(numPoints-1);

        int[] returnIndices = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            returnIndices[i] = indices.get(i);
        }

        return postProcessCorners(points, numPoints, returnIndices, straws);
    }

    public static Vector2[] runShortStraw(Vector2[] points, int numPoints) {
        float s = determineResampleSpacing(points, numPoints);
        int resampledCount = getResampledPoints(points, numPoints, s);
        int[] cornerIndices = getCorners(resampledPoints, resampledCount);

        List<Vector2> finalPoints = new ArrayList<Vector2>();

        for (int i = 0 ; i < cornerIndices.length; i++) {
            finalPoints.add(resampledPoints[cornerIndices[i]]);
        }

        Vector2[] returnArray = new Vector2[finalPoints.size()];
        finalPoints.toArray(returnArray);

        return returnArray;
    }

    private static int[] postProcessCorners(Vector2[] points, int numPoints, int[] corners, float[] straws) {
        List<Integer> cornersList = new ArrayList<Integer>(corners.length);
        for (int i = 0; i < corners.length; i++) {
            cornersList.add(corners[i]);
        }

        boolean go = false;
        int i, c1, c2;

        while (!go) {
            go = true;
            for (i = 1; i < cornersList.size(); i++) {
                c1 = cornersList.get(i-1);
                c2 = cornersList.get(i);
                if (isLine(points, c1, c2)) {
                    int newCorner = halfwayCorner(straws, c1, c2);
                    if (newCorner > c1 && newCorner < c2) {
                        cornersList.add(i, newCorner);
                        go = false;
                    }
                }
            }
        }

        for (i = 1; i < cornersList.size() - 1; i++) {
            c1 = cornersList.get(i-1);
            c2 = cornersList.get(i+1);
            if (isLine(points, c1, c2)) {
                cornersList.remove(i);
                i--;
            }
        }

        int[] returnList = new int[cornersList.size()];

        for (i = 0; i < cornersList.size(); i++) {
            returnList[i] = cornersList.get(i);
        }

        return returnList;
    }

    private static boolean isLine(Vector2[] points, int a, int b) {
        float distance = Vector2.distance(points[a], points[b]);
        float pathDistance = pathDistance(points, a, b);
        return (distance / pathDistance) > lineThreshold;
    }

    private static float pathDistance(Vector2[] points, int a, int b) {
        float d = 0.0f;
        for (int i = a; i < b; i++) {
            d += Vector2.distance(points[i], points[i+1]);
        }

        return d;
    }

    private static int halfwayCorner(float[] straws, int a, int b) {
        int quarter = (b-a) / 4;
        float minValue = Float.POSITIVE_INFINITY;
        int minIndex = 0;

        for (int i = a + quarter; i < (b-quarter); i++) {
            if (straws[i] < minValue) {
                minValue = straws[i];
                minIndex = i;
            }
        }

        return minIndex;
    }
}
