package com.indudhara.dependency.analyzer;

import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.indudhara.dependency.model.DependencyEdge.EdgeType;
import com.indudhara.dependency.model.DependencyGraph;
import com.indudhara.dependency.model.DependencyNode.NodeType;

public class ClassDependencyAnalyzer {
    public void analyze(Map<?, CompilationUnit> compilationUnits, DependencyGraph graph) {
        compilationUnits.values().forEach(compilationUnit ->
                compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                        .forEach(classDeclaration -> analyzeClass(classDeclaration, graph)));
    }

    private void analyzeClass(ClassOrInterfaceDeclaration classDeclaration, DependencyGraph graph) {
        String className = classDeclaration.getNameAsString();
        graph.addNode(className, NodeType.CLASS);

        classDeclaration.getFields().forEach(fieldDeclaration ->
                fieldDeclaration.getVariables().forEach(variable ->
                        addTypeDependency(graph, className, variable.getType(), "field type")));

        classDeclaration.findAll(MethodDeclaration.class).forEach(methodDeclaration -> {
            addTypeDependency(graph, className, methodDeclaration.getType(), "return type");
            methodDeclaration.getParameters().forEach(parameter ->
                    addTypeDependency(graph, className, parameter.getType(), "method parameter type"));
        });

        classDeclaration.findAll(ObjectCreationExpr.class).forEach(objectCreation ->
                graph.addEdge(className, objectCreation.getType().getNameAsString(), EdgeType.CLASS, "object creation"));
    }

    private void addTypeDependency(DependencyGraph graph, String className, Type type, String reason) {
        extractSimpleTypeName(type).ifPresent(typeName -> graph.addEdge(className, typeName, EdgeType.CLASS, reason));
    }

    private Optional<String> extractSimpleTypeName(Type type) {
        if (type.isVoidType() || type.isPrimitiveType()) {
            return Optional.empty();
        }
        if (type.isArrayType()) {
            return extractSimpleTypeName(type.asArrayType().getComponentType());
        }
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType classOrInterfaceType = type.asClassOrInterfaceType();
            return Optional.of(classOrInterfaceType.getNameAsString());
        }
        return Optional.empty();
    }
}
