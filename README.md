# Java Dependency Analyzer

A small Java 17 command-line tool that parses Java source files, extracts simple class-level and method-level dependencies, prints them to the console, and writes a Graphviz DOT file.

## Project Status

Work in progress.

This first version is intentionally AST-based. It does not perform full semantic type resolution and should be treated as a lightweight dependency exploration tool, not a complete compiler-grade analyzer.

## Technologies

- Java 17
- Maven
- JavaParser
- Graphviz DOT export

## Current Features

- Recursively parses `.java` files from a source directory.
- Extracts class-level dependencies from:
  - field types
  - method parameter types
  - method return types
  - object creation expressions
- Extracts basic method-level dependencies from method calls.
- Builds a simple dependency graph model.
- Prints dependency nodes and edges to the console.
- Exports the graph to `dependency-graph.dot`.
- Includes example Java files under `examples/`.

## Planned Features

- Optional JavaParser symbol solver support for better type resolution.
- More precise owner resolution for method calls.
- Package-aware graph filtering.
- JSON export.
- Tests for parser, analyzer, and DOT export behavior.
- Graph rendering instructions or scripts for common Graphviz workflows.

## How to Build

```bash
mvn compile
```

## How to Run

Analyze the included examples:

```bash
mvn exec:java
```

Analyze another directory:

```bash
mvn exec:java -Dexec.args="path/to/java/sources"
```

The DOT export is written to:

```text
dependency-graph.dot
```

If Graphviz is installed, render it with:

```bash
dot -Tpng dependency-graph.dot -o dependency-graph.png
```

## Example Output

```text
Analyzing Java sources in: examples

Dependency nodes:
  CLASS: App
  CLASS: Repository
  CLASS: Service
  METHOD: App.main
  METHOD: Service.process

Dependency edges:
  App -> Service [class: object creation]
  App -> String [class: method parameter type]
  Service -> Repository [class: field type]
  Service -> Repository [class: object creation]
  Service.process -> Repository.findById [method: method call]
  Service.process -> Repository.save [method: method call]
  Service.process -> String.toUpperCase [method: method call]

DOT graph exported to: dependency-graph.dot
```

Actual output may vary as the examples evolve.

## Limitations

- Method call targets are based on syntax and local heuristics only.
- The analyzer does not resolve overloaded methods.
- The analyzer does not fully resolve imports, inheritance, generics, or external libraries.
- Calls like `service.process()` may be reported using the visible scope name when the actual type is not identifiable.
- Nested and anonymous classes are handled only at a basic AST traversal level.
- This tool currently favors readable dependency hints over complete dependency resolution.
