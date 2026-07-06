# Java Dependency Analyzer

A Java 17 command-line tool that parses Java source files, extracts class-level and method-level dependencies, prints them to the console, and writes Graphviz DOT and optional JSON output.

## Project Status

Work in progress.

This first version is intentionally AST-based. It does not perform full semantic type resolution and should be treated as a lightweight dependency exploration tool, not a complete compiler-grade analyzer.

## Technologies

- Java 17
- Maven
- JavaParser
- Graphviz DOT export
- JUnit 5

## Current Features

- Recursively parses `.java` files from a source directory.
- Builds a best-effort source index for project classes and explicit imports.
- Extracts class-level dependencies from:
  - field types
  - method parameter types
  - constructor parameter types
  - method return types
  - implemented interfaces
  - superclasses
  - object creation expressions
- Extracts generic type arguments such as `List<Repository>`.
- Extracts basic method-level dependencies from:
  - method calls
  - static-looking method calls
  - constructor calls
- Includes edge kinds and source line locations in output.
- Builds a simple dependency graph model.
- Prints dependency nodes and edges to the console.
- Exports the graph to `dependency-graph.dot`.
- Optionally exports the graph to JSON.
- Includes more realistic example Java files under `examples/`.
- Includes focused JUnit tests.

## Planned Features

- Optional JavaParser symbol solver support for better type resolution.
- Package-aware graph filtering.
- Better chained-call return type inference.
- Method reference extraction.
- Tests for parser and DOT export behavior.
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

With explicit output files:

```bash
mvn exec:java -Dexec.args="--source examples --dot dependency-graph.dot --json dependency-graph.json"
```

In PowerShell, quote the Maven property as one argument:

```powershell
mvn exec:java '-Dexec.args=--source examples --dot dependency-graph.dot --json dependency-graph.json'
```

Available options:

```text
--source <dir>   Directory containing Java source files. Defaults to examples.
--dot <file>     DOT output file. Defaults to dependency-graph.dot.
--json <file>    Optional JSON output file.
--quiet          Do not print graph nodes and edges to the console.
--help           Show CLI help.
```

The default DOT export is written to:

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
  CLASS: examples.App
  CLASS: examples.Repository
  CLASS: examples.Service
  CLASS: java.util.List
  METHOD: examples.App.main
  METHOD: examples.Service.process

Dependency edges:
  examples.App -> examples.Service [class: object creation @ line 7]
  examples.EmailNotifier -> examples.Notifier [class: implemented interface @ line 5]
  examples.Service -> examples.Repository [class: field type @ line 6]
  examples.Service -> examples.Notifier [class: method parameter type @ line 9]
  examples.Service.process -> examples.Repository.findById [method: method call @ line 15]
  examples.Service.process -> examples.Formatter.normalize [method: static method call @ line 16]
  examples.Service.process -> java.util.List.of [method: static method call @ line 18]

DOT graph exported to: dependency-graph.dot
JSON graph exported to: dependency-graph.json
```

Actual output may vary as the examples evolve.

## Limitations

- Method call targets are based on syntax and local heuristics only.
- The analyzer does not resolve overloaded methods.
- The analyzer resolves project classes and explicit imports, but does not fully resolve wildcard imports, inheritance, generics, or external libraries.
- Calls like `service.process()` may be reported using the visible scope name when the actual type is not identifiable.
- Chained calls like `repository.findById(id).orElse(...)` may produce an unresolved synthetic target because return types are not inferred.
- Method references such as `System.out::println` are not currently extracted as method-call edges.
- Nested and anonymous classes are handled only at a basic AST traversal level.
- This tool currently favors readable dependency hints over complete dependency resolution.
