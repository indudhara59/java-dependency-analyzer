package com.indudhara.dependency.model;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.indudhara.dependency.model.DependencyEdge.EdgeType;
import com.indudhara.dependency.model.DependencyEdge.EdgeKind;
import com.indudhara.dependency.model.DependencyNode.NodeType;

public class DependencyGraph {
    private final Set<DependencyNode> nodes = new LinkedHashSet<>();
    private final Set<DependencyEdge> edges = new LinkedHashSet<>();

    public void addNode(String id, NodeType type) {
        if (id != null && !id.isBlank()) {
            nodes.add(new DependencyNode(id, type));
        }
    }

    public void addEdge(String source, String target, EdgeType type, String reason) {
        addEdge(source, target, type, EdgeKind.valueOf(reason.toUpperCase().replace(' ', '_')), "unknown");
    }

    public void addEdge(String source, String target, EdgeType type, EdgeKind kind, String location) {
        if (source == null || target == null || source.isBlank() || target.isBlank()) {
            return;
        }
        if (source.equals(target)) {
            return;
        }
        NodeType nodeType = type == EdgeType.CLASS ? NodeType.CLASS : NodeType.METHOD;
        addNode(source, nodeType);
        addNode(target, nodeType);
        edges.add(new DependencyEdge(source, target, type, kind, location));
    }

    public Set<DependencyNode> getNodes() {
        return nodes.stream()
                .sorted(Comparator.comparing(DependencyNode::getType).thenComparing(DependencyNode::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<DependencyEdge> getEdges() {
        return edges.stream()
                .sorted(Comparator.comparing(DependencyEdge::getType)
                        .thenComparing(DependencyEdge::getSource)
                        .thenComparing(DependencyEdge::getTarget)
                        .thenComparing(DependencyEdge::getKind)
                        .thenComparing(DependencyEdge::getLocation))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
