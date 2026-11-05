package com.ventooth.modernvk;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.ArrayList;
import java.util.Objects;

@Accessors(fluent = true,
           chain = false)
public final class NodeWorld {
    private final ArrayList<Node> nodes;
    @Getter
    private final int maxNodes;

    public NodeWorld(int maxNodes) {
        this.nodes = new ArrayList<>(maxNodes);
        this.maxNodes = maxNodes;
    }

    public Pair<Node, Integer> createNode() {
        if (nodes.size() >= maxNodes) {
            throw new IllegalStateException("Node world is at capacity");
        }
        val node = new Node();
        nodes.add(node);
        val nodeId = nodes.size();
        return new Pair<>(node, nodeId);
    }

    public Node getNode(int nodeId) {
        if (nodeId < 0) {
            throw new IllegalStateException("Tried retrieving a node with nil ID");
        }
        return Objects.requireNonNull(nodes.get(nodeId - 1));
    }
}