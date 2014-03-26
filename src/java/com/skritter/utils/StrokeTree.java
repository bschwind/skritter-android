package com.skritter.utils;

import com.skritter.models.Param;
import com.skritter.models.Stroke;
import com.skritter.models.StrokeData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrokeTree {
    public class StrokeNode {
        public Stroke stroke;
        public int strokeRank;
        public int numStrokes;
        public boolean hasBeenDrawn;
        public boolean canBeConsidered; // If false, don't even consider when doing stroke recognition 
        private List<StrokeNode> children;
        
        public StrokeNode(Stroke stroke, int strokeRank, int numStrokes) {
            this.stroke = stroke;
            this.strokeRank = strokeRank;
            this.numStrokes = numStrokes;
            this.canBeConsidered = true;
            
            children = new ArrayList<StrokeNode>();
        }
        
        public void addChild(StrokeNode strokeNode) {
            children.add(strokeNode);
        }
        
        public boolean hasChild(int strokeID) {
            for (StrokeNode strokeNode : children) {
                if (strokeNode.stroke.strokeID == strokeID) {
                    return true;
                }
            }
            
            return false;
        }
        
        public StrokeNode getChildWithStrokeID(int strokeID) {
            for (StrokeNode strokeNode : children) {
                if (strokeNode.stroke.strokeID == strokeID) {
                    return strokeNode;
                }
            }

            return null;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof  StrokeNode)) {
                return false;
            }
            
            StrokeNode otherStrokeNode = (StrokeNode)other;
            return this.stroke.equals(otherStrokeNode.stroke)
                    && this.strokeRank == otherStrokeNode.strokeRank
                    && this.numStrokes == otherStrokeNode.numStrokes;
        }
        
        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public String toString() {
            return "\"" + this.stroke.strokeID + "-" + this.strokeRank + "\"";
        }
    }
    
    private StrokeNode sentinel;
    private Map<Integer, List<StrokeNode>> strokeRankMap;
    private List<StrokeNode> strokeNodesList;
    private int maxStrokeNumber;
    
    public StrokeTree(StrokeData strokeData) {
        strokeNodesList = new ArrayList<StrokeNode>();
        
        sentinel = new StrokeNode(null, 0, 0);
        
        strokeRankMap = new HashMap<Integer, List<StrokeNode>>();        
        
        for (int i = 0; i < strokeData.getStrokes().length; i++) {
            Stroke[] variation = strokeData.getStrokes()[i];
            if (variation.length > maxStrokeNumber) {
                maxStrokeNumber = variation.length;
            }
            
            StrokeNode parent = sentinel;
            
            for (int j = 0; j < variation.length; j++) {
                Stroke stroke = variation[j];
                
                int numStrokes = getStrokeCountFromStrokeID(stroke.strokeID);
                int newStrokeRank = parent.strokeRank + numStrokes;
                int nextRank = parent.strokeRank + 1;
                
                StrokeNode potentialNextStrokeNode = parent.getChildWithStrokeID(stroke.strokeID);
                
                if (potentialNextStrokeNode == null) {
                    potentialNextStrokeNode = getStrokeNodeFromRank(nextRank, stroke.strokeID);
                    if (potentialNextStrokeNode != null) {
                        parent.addChild(potentialNextStrokeNode);
                    }
                }
                
                if (potentialNextStrokeNode != null) {
                    parent = potentialNextStrokeNode;
                    continue;
                } else {
                    StrokeNode newStrokeNode = new StrokeNode(stroke, newStrokeRank, numStrokes);
                    strokeNodesList.add(newStrokeNode);
                    parent.addChild(newStrokeNode);
                    insertNewStrokeRank(newStrokeNode, parent.strokeRank);
                    parent = newStrokeNode;
                }
            }
        }
    }
    
    private int getStrokeCountFromStrokeID(int strokeID) {
        for (Param param : Param.params) {
            if (strokeID == param.bitmapID) {
                if (param.containedStrokes != null) {
                    return param.containedStrokes.length;
                } else {
                    return 1;
                }
            }
        }
        
        return 1;
    }
    
    private StrokeNode getStrokeNodeFromRank(int rank, int strokeID) {
        List<StrokeNode> strokeNodes = strokeRankMap.get(rank);
        
        if (strokeNodes == null || strokeNodes.isEmpty()) {
            return null;
        }
        
        for (StrokeNode strokeNode : strokeNodes) {
            if (strokeID == strokeNode.stroke.strokeID) {
                return strokeNode;
            }
        }
        
        return null;
    }
    
    private void insertNewStrokeRank(StrokeNode strokeNode, int parentStrokeRank) {
        int numRanksToInsert = strokeNode.strokeRank - parentStrokeRank;
        for (int i = 0; i < numRanksToInsert; i++) {
            int newRank = parentStrokeRank + i + 1;
            
            List<StrokeNode> nodes = strokeRankMap.get(newRank);
            
            if (nodes != null) {
                nodes.add(strokeNode);
            } else {
                List<StrokeNode> newStrokeNodes = new ArrayList<StrokeNode>();
                newStrokeNodes.add(strokeNode);
                strokeRankMap.put(newRank, newStrokeNodes);
            }
        }
    }
    
    public List<StrokeNode> getStrokeNodesList() {
        return strokeNodesList;
    }
    
    public StrokeNode getNextStroke() {
        List<StrokeNode> nodes = getStrokeNodesToTest(0);
        if (!nodes.isEmpty()) {
            return nodes.get(0);
        } else {
            return null;
        }
    }
    
    public List<StrokeNode> getStrokeNodesToTest(int strokeOrderWindow) {
        List<StrokeNode> nodes = new ArrayList<StrokeNode>();
        int currentStrokeRank = getCurrentStrokeRank();
        
        for (int i = currentStrokeRank; i <= currentStrokeRank + strokeOrderWindow; i++) {
            if (i > maxStrokeNumber) {
                break;
            }
            
            List<StrokeNode> rankNodeList = strokeRankMap.get(i);
            
            if (rankNodeList == null || rankNodeList.isEmpty()) {
                continue;
            }
            
            for (StrokeNode node : rankNodeList) {
                if (!node.canBeConsidered || node.hasBeenDrawn) {
                    continue;
                }
                
                nodes.add(node);
            }
        }
        
        return nodes;
    }
    
    private int getCurrentStrokeRank() {
        for (int i = 1; i <= maxStrokeNumber; i++) {
            for (StrokeNode node : strokeRankMap.get(i)) {
                if (node.hasBeenDrawn || !node.canBeConsidered) {
                    continue;
                }
                
                return i;
            }
        }
        
        return 1;
    }
    
    public void markNodeAsDrawn(StrokeNode node) {
        // Mark node as drawn
        node.hasBeenDrawn = true;
        // Go through all other nodes of node's rank, and mark them as false for "canBeConsidered"
        // For strokes that span multiple ranks, go through each rank and mark all the others as false for "canBeConsidered"
        for (int i = node.strokeRank; i >= node.strokeRank - node.numStrokes + 1; i--) {
            for (StrokeNode strokeNode : strokeRankMap.get(i)) {
                if (!node.equals(strokeNode)) {
                    strokeNode.canBeConsidered = false;
                }
            }
        }
        // Search through the list of nodes, and find the ones that have the same exact stroke positioning,
        // and mark them as false for "canBeConsidered"
        
        for (StrokeNode strokeNode : strokeNodesList) {
            if (!node.equals(strokeNode) && node.stroke.equals(strokeNode.stroke)) {
                strokeNode.canBeConsidered = false;
            }
        }
    }
    
    public boolean characterIsComplete() {
        return characterIsCompleteRecursive(sentinel);
    }

    /**
     * Depth first path search from the sentinel node to the first node without a child.
     * If we find a path, the character is considered complete
     * @param strokeNode
     * @return true if the character is complete, false otherwise
     */
    private boolean characterIsCompleteRecursive(StrokeNode strokeNode) {
        if (strokeNode.children.size() == 0) {
            return true;
        }
        
        boolean result;
        
        for (StrokeNode child : strokeNode.children) {
            if (child.hasBeenDrawn) {
                result = characterIsCompleteRecursive(child);
                if (result) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public String buildStrokeDotFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph StrokeTree {\n\trankdir=LR;\n");
        
        for (StrokeNode node : strokeNodesList) {
            sb.append("\t" + node.toString() + ";\n");
        }
        
        sb.append("\n");
        
        for (StrokeNode node : strokeNodesList) {
            for (StrokeNode child : node.children) {
                sb.append("\t" + node.toString() + "->" + child.toString() + ";\n");
            }
        }
        
        sb.append("}\n");
        
        return sb.toString();
    }
}
