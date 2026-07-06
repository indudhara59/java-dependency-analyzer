package com.indudhara.dependency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.indudhara.dependency.analyzer.ClassDependencyAnalyzer;
import com.indudhara.dependency.analyzer.MethodDependencyAnalyzer;
import com.indudhara.dependency.exporter.DotExporter;
import com.indudhara.dependency.exporter.JsonExporter;
import com.indudhara.dependency.model.DependencyGraph;
import com.indudhara.dependency.parser.ProjectParser;

public class Main {
    public static void main(String[] args) throws IOException {
        CliOptions options = CliOptions.parse(args);

        if (options.help()) {
            printUsage();
            return;
        }

        if (!Files.exists(options.sourceRoot()) || !Files.isDirectory(options.sourceRoot())) {
            System.err.println("Source directory does not exist: " + options.sourceRoot().toAbsolutePath());
            System.exit(1);
        }

        System.out.println("Analyzing Java sources in: " + options.sourceRoot());
        System.out.println();

        ProjectParser parser = new ProjectParser();
        Map<Path, CompilationUnit> compilationUnits = parser.parse(options.sourceRoot());

        DependencyGraph graph = new DependencyGraph();
        new ClassDependencyAnalyzer().analyze(compilationUnits, graph);
        new MethodDependencyAnalyzer().analyze(compilationUnits, graph);

        if (options.printConsole()) {
            printResults(graph);
        }

        String dot = new DotExporter().export(graph);
        Files.writeString(options.dotOutput(), dot);
        System.out.println();
        System.out.println("DOT graph exported to: " + options.dotOutput().toAbsolutePath());

        if (options.jsonOutput() != null) {
            Files.writeString(options.jsonOutput(), new JsonExporter().export(graph));
            System.out.println("JSON graph exported to: " + options.jsonOutput().toAbsolutePath());
        }
    }

    private static void printResults(DependencyGraph graph) {
        System.out.println("Dependency nodes:");
        graph.getNodes().forEach(node -> System.out.println("  " + node));

        System.out.println();
        System.out.println("Dependency edges:");
        graph.getEdges().forEach(edge -> System.out.println("  " + edge));
    }

    private static void printUsage() {
        System.out.println("""
                Java Dependency Analyzer

                Usage:
                  mvn exec:java
                  mvn exec:java -Dexec.args="--source examples --dot dependency-graph.dot --json dependency-graph.json"

                Options:
                  --source <dir>   Directory containing Java source files. Defaults to examples.
                  --dot <file>     DOT output file. Defaults to dependency-graph.dot.
                  --json <file>    Optional JSON output file.
                  --quiet          Do not print graph nodes and edges to the console.
                  --help           Show this help.
                """);
    }

    private record CliOptions(Path sourceRoot, Path dotOutput, Path jsonOutput, boolean printConsole, boolean help) {
        private static CliOptions parse(String[] args) {
            Path sourceRoot = Path.of("examples");
            Path dotOutput = Path.of("dependency-graph.dot");
            Path jsonOutput = null;
            boolean printConsole = true;
            boolean help = false;

            for (int index = 0; index < args.length; index++) {
                String arg = args[index];
                switch (arg) {
                    case "--source" -> sourceRoot = Path.of(requireValue(args, ++index, "--source"));
                    case "--dot" -> dotOutput = Path.of(requireValue(args, ++index, "--dot"));
                    case "--json" -> jsonOutput = Path.of(requireValue(args, ++index, "--json"));
                    case "--quiet" -> printConsole = false;
                    case "--help", "-h" -> help = true;
                    default -> {
                        if (arg.startsWith("--")) {
                            throw new IllegalArgumentException("Unknown option: " + arg);
                        }
                        sourceRoot = Path.of(arg);
                    }
                }
            }

            return new CliOptions(sourceRoot, dotOutput, jsonOutput, printConsole, help);
        }

        private static String requireValue(String[] args, int index, String option) {
            if (index >= args.length) {
                throw new IllegalArgumentException(option + " requires a value");
            }
            return args[index];
        }
    }
}
