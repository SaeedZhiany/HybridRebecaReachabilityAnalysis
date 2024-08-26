package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import stateSpace.HybridState;

import java.util.ArrayList;
import java.util.List;

public class ReachabilityAnalysisGraph {

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

    public ReachabilityAnalysisGraph(HybridState rootData) {
        this.root = new TreeNode("root", rootData);
    }

    public TreeNode getRoot() {
        return root;
    }

    public void addNode(TreeNode parent, String nodeId, HybridState nodeData) {
        TreeNode newNode = new TreeNode(nodeId, nodeData);
        parent.addChild(newNode);
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
