package com.indudhara.dependency.analyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.indudhara.dependency.model.DependencyEdge.EdgeKind;
import com.indudhara.dependency.model.DependencyEdge.EdgeType;
import com.indudhara.dependency.model.DependencyGraph;
import com.indudhara.dependency.model.DependencyNode.NodeType;

public class MethodDependencyAnalyzer {
    public void analyze(Map<?, CompilationUnit> compilationUnits, DependencyGraph graph) {
        SourceIndex sourceIndex = new SourceIndex(compilationUnits.values());
        compilationUnits.values().forEach(compilationUnit ->
                compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                        .forEach(classDeclaration -> analyzeClass(compilationUnit, classDeclaration, sourceIndex, graph)));
    }

    private void analyzeClass(
            CompilationUnit compilationUnit,
            ClassOrInterfaceDeclaration classDeclaration,
            SourceIndex sourceIndex,
            DependencyGraph graph) {
        String className = sourceIndex.qualifiedClassName(compilationUnit, classDeclaration);
        Map<String, String> knownVariableTypes = collectFieldTypes(compilationUnit, classDeclaration, sourceIndex);

        classDeclaration.findAll(ConstructorDeclaration.class).forEach(constructorDeclaration ->
                analyzeCallable(compilationUnit, className, className + ".<init>",
                        constructorDeclaration.getParameters(), constructorDeclaration, knownVariableTypes,
                        sourceIndex, graph));
        classDeclaration.findAll(MethodDeclaration.class).forEach(methodDeclaration ->
                analyzeCallable(compilationUnit, className, className + "." + methodDeclaration.getNameAsString(),
                        methodDeclaration.getParameters(), methodDeclaration, knownVariableTypes, sourceIndex, graph));
    }

    private Map<String, String> collectFieldTypes(
            CompilationUnit compilationUnit,
            ClassOrInterfaceDeclaration classDeclaration,
            SourceIndex sourceIndex) {
        Map<String, String> fieldTypes = new HashMap<>();
        classDeclaration.getFields().forEach(fieldDeclaration ->
                fieldDeclaration.getVariables().forEach(variable ->
                        sourceIndex.resolveType(compilationUnit, variable.getType())
                                .ifPresent(type -> fieldTypes.put(variable.getNameAsString(), type))));
        return fieldTypes;
    }

    private void analyzeCallable(
            CompilationUnit compilationUnit,
            String className,
            String caller,
            NodeList<Parameter> parameters,
            Node searchRoot,
            Map<String, String> knownFieldTypes,
            SourceIndex sourceIndex,
            DependencyGraph graph) {
        graph.addNode(caller, NodeType.METHOD);

        Map<String, String> knownVariableTypes = new HashMap<>(knownFieldTypes);
        parameters.forEach(parameter ->
                sourceIndex.resolveType(compilationUnit, parameter.getType())
                        .ifPresent(type -> knownVariableTypes.put(parameter.getNameAsString(), type)));
        searchRoot.findAll(VariableDeclarationExpr.class).forEach(variableDeclaration ->
                variableDeclaration.getVariables().forEach(variable ->
                        sourceIndex.resolveType(compilationUnit, variable.getType())
                                .ifPresent(type -> knownVariableTypes.put(variable.getNameAsString(), type))));

        searchRoot.findAll(ObjectCreationExpr.class).forEach(objectCreation -> {
            String constructor = sourceIndex.resolveSimpleName(compilationUnit, objectCreation.getType().getNameAsString())
                    + ".<init>";
            graph.addEdge(caller, constructor, EdgeType.METHOD, EdgeKind.CONSTRUCTOR_CALL,
                    SourceIndex.location(objectCreation));
        });

        searchRoot.findAll(MethodCallExpr.class).forEach(methodCall -> {
            ResolvedCall resolvedCall = resolveCalledMethod(compilationUnit, className, methodCall,
                    knownVariableTypes, sourceIndex);
            graph.addEdge(caller, resolvedCall.methodName(), EdgeType.METHOD, resolvedCall.kind(),
                    SourceIndex.location(methodCall));
        });
    }

    private ResolvedCall resolveCalledMethod(
            CompilationUnit compilationUnit,
            String currentClassName,
            MethodCallExpr methodCall,
            Map<String, String> knownVariableTypes,
            SourceIndex sourceIndex) {
        Optional<Expression> scope = methodCall.getScope();
        if (scope.isEmpty()) {
            return new ResolvedCall(currentClassName + "." + methodCall.getNameAsString(), EdgeKind.METHOD_CALL);
        }

        Expression scopeExpression = scope.get();
        if (scopeExpression instanceof NameExpr nameExpression) {
            String scopeName = nameExpression.getNameAsString();
            if (knownVariableTypes.containsKey(scopeName)) {
                return new ResolvedCall(knownVariableTypes.get(scopeName) + "." + methodCall.getNameAsString(),
                        EdgeKind.METHOD_CALL);
            }
            String owner = sourceIndex.resolveSimpleName(compilationUnit, scopeName);
            EdgeKind kind = Character.isUpperCase(scopeName.charAt(0))
                    ? EdgeKind.STATIC_METHOD_CALL
                    : EdgeKind.UNRESOLVED_METHOD_CALL;
            return new ResolvedCall(owner + "." + methodCall.getNameAsString(), kind);
        }

        if (scopeExpression instanceof ObjectCreationExpr objectCreationExpr) {
            String owner = sourceIndex.resolveSimpleName(compilationUnit, objectCreationExpr.getType().getNameAsString());
            return new ResolvedCall(owner + "." + methodCall.getNameAsString(), EdgeKind.METHOD_CALL);
        }

        return new ResolvedCall(scopeExpression + "." + methodCall.getNameAsString(), EdgeKind.UNRESOLVED_METHOD_CALL);
    }

    private record ResolvedCall(String methodName, EdgeKind kind) {
    }
}
