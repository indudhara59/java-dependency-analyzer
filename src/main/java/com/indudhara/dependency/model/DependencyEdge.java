package com.indudhara.dependency.model;

import java.util.Objects;

public final class DependencyEdge {
    private final String source;
    private final String target;
    private final EdgeType type;
    private final EdgeKind kind;
    private final String location;

    public DependencyEdge(String source, String target, EdgeType type, EdgeKind kind, String location) {
        this.source = Objects.requireNonNull(source, "source");
        this.target = Objects.requireNonNull(target, "target");
        this.type = Objects.requireNonNull(type, "type");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.location = location == null ? "unknown" : location;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public EdgeType getType() {
        return type;
    }

    public EdgeKind getKind() {
        return kind;
    }

    public String getReason() {
        return kind.getLabel();
    }

    public String getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DependencyEdge that)) {
            return false;
        }
        return source.equals(that.source)
                && target.equals(that.target)
                && type == that.type
                && kind == that.kind
                && location.equals(that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, type, kind, location);
    }

    @Override
    public String toString() {
        return source + " -> " + target + " [" + type.label + ": " + kind.label + " @ " + location + "]";
    }

    public enum EdgeType {
        CLASS("class"),
        METHOD("method");

        private final String label;

        EdgeType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum EdgeKind {
        FIELD_TYPE("field type"),
        METHOD_PARAMETER_TYPE("method parameter type"),
        RETURN_TYPE("return type"),
        SUPERCLASS("superclass"),
        IMPLEMENTED_INTERFACE("implemented interface"),
        OBJECT_CREATION("object creation"),
        CONSTRUCTOR_CALL("constructor call"),
        METHOD_CALL("method call"),
        STATIC_METHOD_CALL("static method call"),
        UNRESOLVED_METHOD_CALL("unresolved method call");

        private final String label;

        EdgeKind(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
