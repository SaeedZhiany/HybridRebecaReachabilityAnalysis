package utils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamWriteConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.tuple.Pair;
import stateSpace.HybridState;

import java.util.*;


public class ReachabilityAnalysisGraph {

    // HashMap to store the nodes by their IDs
    private Map<String, TreeNode> nodeMap;
    private Map<Integer, String> intToNodeMap;
    private Map<String, Integer> nodeToIntMap;
    private Integer counter;
    private Map<Integer, Pair<Integer, String>> edges;

    // Root node of the graph
    private TreeNode root;

    public ReachabilityAnalysisGraph(HybridState rootData) {
        this.root = new TreeNode(rootData.getHash(), rootData);
        this.nodeMap = new HashMap<>();
        this.intToNodeMap = new HashMap<>();
        this.nodeToIntMap = new HashMap<>();
        this.edges = new HashMap<>();
        this.nodeMap.put(root.getId(), root); // Add root to the map
        this.counter = 0;
        this.intToNodeMap.put(this.counter, rootData.getHash());
        this.nodeToIntMap.put(rootData.getHash(), this.counter);
        this.counter++;
    }

    // Method to find a node in the graph by its ID
    public TreeNode findNodeInGraph(HybridState hybridState) {
        return nodeMap.get(hybridState.getHash());
    }

    // TreeNode class with parent-child relationships
    public static class TreeNode {
        private String id;
        private HybridState data;

        @JsonManagedReference // Manage serialization for the parent-child relationship
        private List<TreeNode> children;

        @JsonBackReference // Avoid recursion by not serializing parent in the child object
        private TreeNode parent;

        public TreeNode(String id, HybridState data) {
            this.id = id;
            this.data = data;
            this.children = new ArrayList<>();
        }

        // Add a child node and set its parent
        public void addChild(TreeNode child) {
            this.children.add(child);
            child.setParent(this);
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

        public TreeNode getParent() {
            return parent;
        }

        public void setParent(TreeNode parent) {
            this.parent = parent;
        }
    }

    public TreeNode getRoot() {
        return root;
    }

    // Add a node to the graph and map
    public void addNode(TreeNode parent, HybridState nodeData, String message) {
        TreeNode newNode = new TreeNode(nodeData.getHash(), nodeData);
        parent.addChild(newNode);
        nodeMap.put(nodeData.getHash(), newNode); // Add the new node to the map
        intToNodeMap.put(this.counter, nodeData.getHash());
        nodeToIntMap.put(nodeData.getHash(), this.counter);
        Integer parentIndex = this.nodeToIntMap.get(parent.getId()), childIndex = this.nodeToIntMap.get(nodeData.getHash());
        this.edges.put(parentIndex, Pair.of(childIndex, message));
        this.counter++;
    }

    // Convert the root node and its children to JSON
    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();

        // Increase the maximum nesting depth
        StreamWriteConstraints constraints = StreamWriteConstraints.builder().maxNestingDepth(Integer.MAX_VALUE).build();
        mapper.getFactory().setStreamWriteConstraints(constraints);

        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting tree to JSON", e);
        }
    }

    public String toDot() {
        String buffer = "";
        buffer += "digraph g {\n" +
                  "  edge [lblstyle=\"above, sloped\"];";

        Map<Integer, String> sortedIntToNodeMap = new TreeMap<>(intToNodeMap);
        String templateIntToNodeMap = "  s%s [shape=\"square\" label=\"%s\"];\n";
        for (Map.Entry<Integer, String> entry : sortedIntToNodeMap.entrySet()) {
            buffer += String.format(templateIntToNodeMap, entry.getKey(), entry.getKey());
        }

        Map<Integer, Pair<Integer, String>> sortedEdges = new TreeMap<>(edges);
        String templateEdges = "  s%s -> s%s [label=\"%s\"];\n";
        for (Map.Entry<Integer, Pair<Integer, String>> entry : sortedEdges.entrySet()) {
            buffer += String.format(templateEdges, entry.getKey(), entry.getValue().getKey(), entry.getValue().getValue());
        }

        buffer += "  __start0 [label=\"\" shape=\"none\" width=\"0\" height=\"0\"];\n" +
                  "  __start0 -> s0;\n}";
        return buffer;
    }
}
