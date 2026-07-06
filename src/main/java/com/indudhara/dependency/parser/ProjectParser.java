package com.indudhara.dependency.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class ProjectParser {
    public Map<Path, CompilationUnit> parse(Path sourceRoot) throws IOException {
        Map<Path, CompilationUnit> parsedFiles = new LinkedHashMap<>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .forEach(path -> parseFile(path, parsedFiles));
        }

        return parsedFiles;
    }

    private void parseFile(Path path, Map<Path, CompilationUnit> parsedFiles) {
        try {
            parsedFiles.put(path, StaticJavaParser.parse(path));
        } catch (IOException | RuntimeException exception) {
            System.err.println("Skipping " + path + ": " + exception.getMessage());
        }
    }
}
