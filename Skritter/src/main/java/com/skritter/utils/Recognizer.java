package com.skritter.utils;

import android.graphics.Matrix;

import com.skritter.math.BoundingBox;
import com.skritter.math.MathUtil;
import com.skritter.models.Param;
import com.skritter.models.Stroke;
import com.skritter.models.StrokeData;
import com.skritter.math.Vector2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class Recognizer {
    private static final float angleThreshold = 30.0f;
    private static float distanceThreshold;
    private static float lengthThreshold;
    private static int orderStrictness;
    private static int strokeOrderWindow = 0; // How many strokes past the "correct" stroke are you allowed to draw?
    private static LinkedHashMap<StrokeTree.StrokeNode, List<Param>> potentialMatches = new LinkedHashMap<StrokeTree.StrokeNode, List<Param>>();
    
    // Return the bitmap ID of the recognized stroke
    public static Stroke recognizeStroke(Vector2[] points, int numPoints, StrokeTree strokeTree, StrokeData strokeData, Set<Param> relevantParams, int canvasSize) {
        distanceThreshold = 150.0f * (canvasSize / 600.0f);
        lengthThreshold = 300.0f * (canvasSize / 600.0f);
        orderStrictness = 0;
        
        int strokeIndex = -1;
        
        Vector2[] corners = ShortStraw.runShortStraw(points, numPoints);
        
        // Build up our list of potential matches
        List<StrokeTree.StrokeNode> nodes = strokeTree.getStrokeNodesToTest(strokeOrderWindow);
        potentialMatches.clear();
        
        for (StrokeTree.StrokeNode strokeNode : nodes) {
            List<Param> params = new ArrayList<Param>();
            
            for (Param param : relevantParams) {
                if (strokeNode.stroke.strokeID != param.bitmapID) {
                    continue;
                }                
                
                params.add(param);
            }
            
            potentialMatches.put(strokeNode, params);
        }
        
        if (potentialMatches.isEmpty()) {
            return null;
        }

        // Filter out matches that don't meet the threshold requirements
        Iterator<StrokeTree.StrokeNode> nodeIterator = potentialMatches.keySet().iterator();
        
        while (nodeIterator.hasNext()) {
            StrokeTree.StrokeNode node = nodeIterator.next();
            boolean paramMatches = false;
            
            for (Param param : potentialMatches.get(node)) {
                // Check param against corners
                if (strokeMeetsThresholds(corners, node.stroke, param, canvasSize)) {
                    paramMatches = true;
                    break;
                }
            }
            
            if (!paramMatches) {
                nodeIterator.remove();
            }
        }
        
        // What remains in potentialMatches at this point are the possible StrokeNodes
        // Pick the first one in the list, if one exists
        
        if (potentialMatches.isEmpty()) {
            return null;
        } else {
            StrokeTree.StrokeNode returnStroke = potentialMatches.keySet().iterator().next();
            strokeTree.markNodeAsDrawn(returnStroke);
            return returnStroke.stroke;
        }
        
//        
//        for (int i = 0; i < strokeData.getStrokes().length; i++) {
//            Stroke stroke = strokeData.getStrokes()[i][0]; // this is bad
//            
//            if (stroke.hasBeenDrawn) {
//                continue;
//            }
//
//            for (int j = 0; j < Param.params.length; j++) {
//                Param param = Param.params[j];
//                
//                if (stroke.strokeID == param.bitmapID) {
//                    if (corners.length == param.corners.length) {
//                        stroke.hasBeenDrawn = true;
//                        return stroke.strokeID;
//                    }
//                }
//            }
//        }
//        
//        return strokeIndex;
    }
    
    private static boolean strokeMeetsThresholds(Vector2[] corners, Stroke stroke, Param param, int canvasSize) {
        float scaleX = (stroke.width * canvasSize) / param.bitmapWidth;
        float scaleY = (stroke.height * canvasSize) / param.bitmapHeight;
        float x = stroke.x * canvasSize;
        float y = stroke.y * canvasSize;
        
        Matrix matrix = new Matrix();
        // Move the bitmap so we can rotate and scale around the center of the image
        matrix.setTranslate(-param.bitmapWidth * 0.5f, -param.bitmapHeight * 0.5f);
        matrix.postRotate(stroke.rotation);
        matrix.postScale(scaleX, scaleY);
        // Move it back so we draw with the bitmap's upper left corner at (x, y)
        matrix.postTranslate(param.bitmapWidth * 0.5f * scaleX, param.bitmapHeight * 0.5f * scaleY);
        matrix.postTranslate(x, y);
        
        // Check Corners
        Vector2[] transformedParamCorners = MathUtil.transformVectorArray(matrix, param.corners);

        int cornerDiff = Math.abs(corners.length - transformedParamCorners.length);
        
        if (cornerDiff > 1) {
            return false;
        }
        
        // Check angles
        float drawnAngle = MathUtil.angleOfPoints(corners);
        drawnAngle = MathUtil.toDegrees(drawnAngle);
        float paramAngle = MathUtil.angleOfPoints(transformedParamCorners);
        paramAngle = MathUtil.toDegrees(paramAngle);
        
        if (Math.abs(drawnAngle - paramAngle) > angleThreshold) {
            return false;
        }
        
        // Check length
        float drawnLength = MathUtil.lengthOfPoints(corners);
        float paramLength = MathUtil.lengthOfPoints(transformedParamCorners);
        
        if (Math.abs(drawnLength - paramLength) > lengthThreshold) {
            return false;
        }
        
        // Check Distance
        BoundingBox drawnBox = BoundingBox.getBounds(corners, corners.length);
        BoundingBox paramBox = BoundingBox.getBounds(transformedParamCorners, transformedParamCorners.length);
        Vector2 midpoint1 = new Vector2(drawnBox.x + drawnBox.width * 0.5f, drawnBox.y + drawnBox.height * 0.5f);
        Vector2 midpoint2 = new Vector2(paramBox.x + paramBox.width * 0.5f, paramBox.y + paramBox.height * 0.5f);
        float distance = Vector2.distance(midpoint1, midpoint2);
        
        if (distance > distanceThreshold) {
            return false;
        }
        
        return true;
    }
}
