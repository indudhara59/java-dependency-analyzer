package com.indudhara.dependency.exporter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.indudhara.dependency.model.DependencyEdge.EdgeKind;
import com.indudhara.dependency.model.DependencyEdge.EdgeType;
import com.indudhara.dependency.model.DependencyGraph;

class JsonExporterTest {
    @Test
    void exportsNodesEdgesKindsAndLocations() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge("examples.Service", "examples.Repository", EdgeType.CLASS, EdgeKind.FIELD_TYPE, "line 6");

        String json = new JsonExporter().export(graph);

        assertTrue(json.contains("\"id\": \"examples.Service\""));
        assertTrue(json.contains("\"target\": \"examples.Repository\""));
        assertTrue(json.contains("\"kind\": \"FIELD_TYPE\""));
        assertTrue(json.contains("\"location\": \"line 6\""));
    }
}
