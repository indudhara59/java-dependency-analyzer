package com.indudhara.dependency.analyzer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.indudhara.dependency.model.DependencyEdge.EdgeKind;
import com.indudhara.dependency.model.DependencyGraph;

class ClassDependencyAnalyzerTest {
    @Test
    void extractsGenericAndImplementedInterfaceDependencies() {
        CompilationUnit unit = StaticJavaParser.parse("""
                package sample;

                import java.util.List;

                interface Worker {}

                class Service implements Worker {
                    private List<Repository> repositories;

                    Repository find(String id) {
                        return new Repository();
                    }
                }

                class Repository {}
                """);
        DependencyGraph graph = new DependencyGraph();

        new ClassDependencyAnalyzer().analyze(Map.of("Service.java", unit), graph);

        assertTrue(graph.getEdges().stream().anyMatch(edge ->
                edge.getSource().equals("sample.Service")
                        && edge.getTarget().equals("sample.Worker")
                        && edge.getKind() == EdgeKind.IMPLEMENTED_INTERFACE));
        assertTrue(graph.getEdges().stream().anyMatch(edge ->
                edge.getSource().equals("sample.Service")
                        && edge.getTarget().equals("java.util.List")
                        && edge.getKind() == EdgeKind.FIELD_TYPE));
        assertTrue(graph.getEdges().stream().anyMatch(edge ->
                edge.getSource().equals("sample.Service")
                        && edge.getTarget().equals("sample.Repository")
                        && edge.getKind() == EdgeKind.FIELD_TYPE));
    }
}
