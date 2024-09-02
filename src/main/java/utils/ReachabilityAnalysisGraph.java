package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import stateSpace.HybridState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReachabilityAnalysisGraph {

    // HashMap to store the nodes by their IDs
    private Map<String, TreeNode> nodeMap;

    public ReachabilityAnalysisGraph(HybridState rootData) {
        this.root = new TreeNode(rootData.getHash(), rootData);
        this.nodeMap = new HashMap<>();
        this.nodeMap.put(root.getId(), root); // Add root to the map
    }

    // Method to find a node in the graph by its ID
    public TreeNode findNodeInGraph(HybridState hybridState) {
        return nodeMap.get(hybridState.getHash());
    }

    public static class TreeNode {
        private String id;
        private HybridState data;
        private List<TreeNode> children;

        public TreeNode(String id, HybridState data) {
            this.id = id;
            this.data = data;
            this.children = new ArrayList<>();
        }

        public void addChild(TreeNode child) {
            this.children.add(child);
        }

        public String getId() {
            return id;
        }

        public HybridState getData() {
            return data;
        }

        public List<TreeNode> getChildren() {
            return children;
        }
    }

    private TreeNode root;

    public TreeNode getRoot() {
        return root;
    }

    // Modified addNode method to add the node to the HashMap
    public void addNode(TreeNode parent, HybridState nodeData) {
        TreeNode newNode = new TreeNode(nodeData.getHash(), nodeData);
        parent.addChild(newNode);
        nodeMap.put(nodeData.getHash(), newNode); // Add the new node to the map
    }

    public String toJson() {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting tree to JSON", e);
        }
    }
}
