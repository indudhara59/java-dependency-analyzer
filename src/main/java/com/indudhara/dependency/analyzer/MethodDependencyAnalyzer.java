package com.indudhara.dependency.analyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.indudhara.dependency.model.DependencyEdge.EdgeType;
import com.indudhara.dependency.model.DependencyGraph;
import com.indudhara.dependency.model.DependencyNode.NodeType;

public class MethodDependencyAnalyzer {
    public void analyze(Map<?, CompilationUnit> compilationUnits, DependencyGraph graph) {
        compilationUnits.values().forEach(compilationUnit ->
                compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                        .forEach(classDeclaration -> analyzeClass(classDeclaration, graph)));
    }

    private void analyzeClass(ClassOrInterfaceDeclaration classDeclaration, DependencyGraph graph) {
        String className = classDeclaration.getNameAsString();
        Map<String, String> knownVariableTypes = collectFieldTypes(classDeclaration);

        classDeclaration.findAll(MethodDeclaration.class).forEach(methodDeclaration ->
                analyzeMethod(className, methodDeclaration, knownVariableTypes, graph));
    }

    private Map<String, String> collectFieldTypes(ClassOrInterfaceDeclaration classDeclaration) {
        Map<String, String> fieldTypes = new HashMap<>();
        classDeclaration.getFields().forEach(fieldDeclaration ->
                fieldDeclaration.getVariables().forEach(variable ->
                        fieldTypes.put(variable.getNameAsString(), variable.getType().asString())));
        return fieldTypes;
    }

    private void analyzeMethod(
            String className,
            MethodDeclaration methodDeclaration,
            Map<String, String> knownFieldTypes,
            DependencyGraph graph) {
        String caller = className + "." + methodDeclaration.getNameAsString();
        graph.addNode(caller, NodeType.METHOD);

        Map<String, String> knownVariableTypes = new HashMap<>(knownFieldTypes);
        methodDeclaration.getParameters().forEach(parameter ->
                knownVariableTypes.put(parameter.getNameAsString(), parameter.getType().asString()));
        methodDeclaration.findAll(VariableDeclarationExpr.class).forEach(variableDeclaration ->
                variableDeclaration.getVariables().forEach(variable ->
                        knownVariableTypes.put(variable.getNameAsString(), variable.getType().asString())));

        methodDeclaration.findAll(MethodCallExpr.class).forEach(methodCall -> {
            String calledMethod = resolveCalledMethod(className, methodCall, knownVariableTypes);
            graph.addEdge(caller, calledMethod, EdgeType.METHOD, "method call");
        });
    }

    private String resolveCalledMethod(
            String currentClassName,
            MethodCallExpr methodCall,
            Map<String, String> knownVariableTypes) {
        Optional<Expression> scope = methodCall.getScope();
        if (scope.isEmpty()) {
            return currentClassName + "." + methodCall.getNameAsString();
        }

        Expression scopeExpression = scope.get();
        if (scopeExpression instanceof NameExpr nameExpression) {
            String scopeName = nameExpression.getNameAsString();
            String owner = knownVariableTypes.getOrDefault(scopeName, scopeName);
            return simplifyType(owner) + "." + methodCall.getNameAsString();
        }

        if (scopeExpression instanceof ObjectCreationExpr objectCreationExpr) {
            return objectCreationExpr.getType().getNameAsString() + "." + methodCall.getNameAsString();
        }

        return scopeExpression + "." + methodCall.getNameAsString();
    }

    private String simplifyType(String type) {
        int genericStart = type.indexOf('<');
        String withoutGenerics = genericStart >= 0 ? type.substring(0, genericStart) : type;
        int lastDot = withoutGenerics.lastIndexOf('.');
        return lastDot >= 0 ? withoutGenerics.substring(lastDot + 1) : withoutGenerics;
    }
}
