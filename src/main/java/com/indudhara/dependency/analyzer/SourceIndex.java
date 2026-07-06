package com.indudhara.dependency.analyzer;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

public class SourceIndex {
    private final Map<String, String> projectTypes = new HashMap<>();

    public SourceIndex(Iterable<CompilationUnit> compilationUnits) {
        for (CompilationUnit compilationUnit : compilationUnits) {
            String packageName = compilationUnit.getPackageDeclaration()
                    .map(packageDeclaration -> packageDeclaration.getNameAsString())
                    .orElse("");
            compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(type -> {
                String simpleName = type.getNameAsString();
                String qualifiedName = packageName.isBlank() ? simpleName : packageName + "." + simpleName;
                projectTypes.put(simpleName, qualifiedName);
            });
        }
    }

    public String qualifiedClassName(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration declaration) {
        String simpleName = declaration.getNameAsString();
        return compilationUnit.getPackageDeclaration()
                .map(packageDeclaration -> packageDeclaration.getNameAsString() + "." + simpleName)
                .orElse(simpleName);
    }

    public Optional<String> resolveType(CompilationUnit compilationUnit, Type type) {
        return resolveTypes(compilationUnit, type).stream().findFirst();
    }

    public Set<String> resolveTypes(CompilationUnit compilationUnit, Type type) {
        Set<String> types = new LinkedHashSet<>();
        if (type.isVoidType() || type.isPrimitiveType()) {
            return types;
        }
        if (type.isArrayType()) {
            return resolveTypes(compilationUnit, type.asArrayType().getComponentType());
        }
        if (type.isClassOrInterfaceType()) {
            type.findAll(ClassOrInterfaceType.class).forEach(discoveredType ->
                    types.add(resolveClassOrInterfaceType(compilationUnit, discoveredType)));
        }
        return types;
    }

    public String resolveSimpleName(CompilationUnit compilationUnit, String typeName) {
        String simpleName = stripGenericsAndArray(typeName);
        if (simpleName.contains(".")) {
            return simpleName;
        }
        if (projectTypes.containsKey(simpleName)) {
            return projectTypes.get(simpleName);
        }
        for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
            if (!importDeclaration.isAsterisk() && importDeclaration.getName().getIdentifier().equals(simpleName)) {
                return importDeclaration.getNameAsString();
            }
        }
        return simpleName;
    }

    public static String location(Node node) {
        return node.getRange()
                .map(range -> "line " + range.begin.line)
                .orElse("unknown");
    }

    private String resolveClassOrInterfaceType(CompilationUnit compilationUnit, ClassOrInterfaceType type) {
        return resolveSimpleName(compilationUnit, type.getNameAsString());
    }

    private String stripGenericsAndArray(String typeName) {
        String withoutArray = typeName.replace("[]", "");
        int genericStart = withoutArray.indexOf('<');
        return genericStart >= 0 ? withoutArray.substring(0, genericStart) : withoutArray;
    }
}
