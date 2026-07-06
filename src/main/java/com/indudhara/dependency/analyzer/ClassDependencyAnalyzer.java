package com.indudhara.dependency.analyzer;

import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.Type;
import com.indudhara.dependency.model.DependencyEdge.EdgeKind;
import com.indudhara.dependency.model.DependencyEdge.EdgeType;
import com.indudhara.dependency.model.DependencyGraph;
import com.indudhara.dependency.model.DependencyNode.NodeType;

public class ClassDependencyAnalyzer {
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
        graph.addNode(className, NodeType.CLASS);

        classDeclaration.getExtendedTypes().forEach(extendedType ->
                graph.addEdge(className, sourceIndex.resolveSimpleName(compilationUnit, extendedType.getNameAsString()),
                        EdgeType.CLASS, EdgeKind.SUPERCLASS, SourceIndex.location(extendedType)));
        classDeclaration.getImplementedTypes().forEach(implementedType ->
                graph.addEdge(className, sourceIndex.resolveSimpleName(compilationUnit, implementedType.getNameAsString()),
                        EdgeType.CLASS, EdgeKind.IMPLEMENTED_INTERFACE, SourceIndex.location(implementedType)));

        classDeclaration.getFields().forEach(fieldDeclaration ->
                fieldDeclaration.getVariables().forEach(variable ->
                        addTypeDependency(compilationUnit, sourceIndex, graph, className, variable.getType(),
                                EdgeKind.FIELD_TYPE, SourceIndex.location(variable))));

        classDeclaration.findAll(MethodDeclaration.class).forEach(methodDeclaration -> {
            addTypeDependency(compilationUnit, sourceIndex, graph, className, methodDeclaration.getType(),
                    EdgeKind.RETURN_TYPE, SourceIndex.location(methodDeclaration));
            methodDeclaration.getParameters().forEach(parameter ->
                    addTypeDependency(compilationUnit, sourceIndex, graph, className, parameter.getType(),
                            EdgeKind.METHOD_PARAMETER_TYPE, SourceIndex.location(parameter)));
        });

        classDeclaration.findAll(ConstructorDeclaration.class).forEach(constructorDeclaration ->
                constructorDeclaration.getParameters().forEach(parameter ->
                        addTypeDependency(compilationUnit, sourceIndex, graph, className, parameter.getType(),
                                EdgeKind.METHOD_PARAMETER_TYPE, SourceIndex.location(parameter))));

        classDeclaration.findAll(ObjectCreationExpr.class).forEach(objectCreation ->
                graph.addEdge(className,
                        sourceIndex.resolveSimpleName(compilationUnit, objectCreation.getType().getNameAsString()),
                        EdgeType.CLASS,
                        EdgeKind.OBJECT_CREATION,
                        SourceIndex.location(objectCreation)));
    }

    private void addTypeDependency(
            CompilationUnit compilationUnit,
            SourceIndex sourceIndex,
            DependencyGraph graph,
            String className,
            Type type,
            EdgeKind kind,
            String location) {
        sourceIndex.resolveTypes(compilationUnit, type).forEach(typeName ->
                graph.addEdge(className, typeName, EdgeType.CLASS, kind, location));
    }
}
