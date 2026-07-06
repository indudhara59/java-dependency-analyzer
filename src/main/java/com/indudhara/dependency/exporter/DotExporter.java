package com.indudhara.dependency.exporter;

import com.indudhara.dependency.model.DependencyEdge;
import com.indudhara.dependency.model.DependencyGraph;
import com.indudhara.dependency.model.DependencyNode;

public class DotExporter {
    public String export(DependencyGraph graph) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph JavaDependencies {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=box, style=rounded];\n\n");

        for (DependencyNode node : graph.getNodes()) {
            dot.append("  \"")
                    .append(escape(node.getId()))
                    .append("\" [label=\"")
                    .append(escape(node.getId()))
                    .append("\\n")
                    .append(node.getType())
                    .append("\"];\n");
        }

        dot.append("\n");

        for (DependencyEdge edge : graph.getEdges()) {
            dot.append("  \"")
                    .append(escape(edge.getSource()))
                    .append("\" -> \"")
                    .append(escape(edge.getTarget()))
                    .append("\" [label=\"")
                    .append(edge.getType().getLabel())
                    .append(": ")
                    .append(escape(edge.getReason()))
                    .append("\"];\n");
        }

        dot.append("}\n");
        return dot.toString();
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
