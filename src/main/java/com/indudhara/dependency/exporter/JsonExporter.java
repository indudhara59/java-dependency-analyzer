package com.indudhara.dependency.exporter;

import com.indudhara.dependency.model.DependencyEdge;
import com.indudhara.dependency.model.DependencyGraph;
import com.indudhara.dependency.model.DependencyNode;

public class JsonExporter {
    public String export(DependencyGraph graph) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"nodes\": [\n");

        int nodeIndex = 0;
        for (DependencyNode node : graph.getNodes()) {
            if (nodeIndex++ > 0) {
                json.append(",\n");
            }
            json.append("    {")
                    .append("\"id\": \"").append(escape(node.getId())).append("\", ")
                    .append("\"type\": \"").append(node.getType()).append("\"")
                    .append("}");
        }

        json.append("\n  ],\n");
        json.append("  \"edges\": [\n");

        int edgeIndex = 0;
        for (DependencyEdge edge : graph.getEdges()) {
            if (edgeIndex++ > 0) {
                json.append(",\n");
            }
            json.append("    {")
                    .append("\"source\": \"").append(escape(edge.getSource())).append("\", ")
                    .append("\"target\": \"").append(escape(edge.getTarget())).append("\", ")
                    .append("\"type\": \"").append(edge.getType()).append("\", ")
                    .append("\"kind\": \"").append(edge.getKind()).append("\", ")
                    .append("\"location\": \"").append(escape(edge.getLocation())).append("\"")
                    .append("}");
        }

        json.append("\n  ]\n");
        json.append("}\n");
        return json.toString();
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
