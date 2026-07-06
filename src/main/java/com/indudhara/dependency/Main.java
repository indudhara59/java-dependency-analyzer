package com.indudhara.dependency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.indudhara.dependency.analyzer.ClassDependencyAnalyzer;
import com.indudhara.dependency.analyzer.MethodDependencyAnalyzer;
import com.indudhara.dependency.exporter.DotExporter;
import com.indudhara.dependency.model.DependencyGraph;
import com.indudhara.dependency.parser.ProjectParser;

public class Main {
    public static void main(String[] args) throws IOException {
        Path sourceRoot = args.length > 0 ? Path.of(args[0]) : Path.of("examples");
        Path dotOutput = Path.of("dependency-graph.dot");

        if (!Files.exists(sourceRoot) || !Files.isDirectory(sourceRoot)) {
            System.err.println("Source directory does not exist: " + sourceRoot.toAbsolutePath());
            System.exit(1);
        }

        System.out.println("Analyzing Java sources in: " + sourceRoot);
        System.out.println();

        ProjectParser parser = new ProjectParser();
        Map<Path, CompilationUnit> compilationUnits = parser.parse(sourceRoot);

        DependencyGraph graph = new DependencyGraph();
        new ClassDependencyAnalyzer().analyze(compilationUnits, graph);
        new MethodDependencyAnalyzer().analyze(compilationUnits, graph);

        printResults(graph);

        String dot = new DotExporter().export(graph);
        Files.writeString(dotOutput, dot);
        System.out.println();
        System.out.println("DOT graph exported to: " + dotOutput.toAbsolutePath());
    }

    private static void printResults(DependencyGraph graph) {
        System.out.println("Dependency nodes:");
        graph.getNodes().forEach(node -> System.out.println("  " + node));

        System.out.println();
        System.out.println("Dependency edges:");
        graph.getEdges().forEach(edge -> System.out.println("  " + edge));
    }
}
