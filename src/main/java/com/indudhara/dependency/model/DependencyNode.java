package com.indudhara.dependency.model;

import java.util.Objects;

public final class DependencyNode {
    private final String id;
    private final NodeType type;

    public DependencyNode(String id, NodeType type) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
    }

    public String getId() {
        return id;
    }

    public NodeType getType() {
        return type;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DependencyNode that)) {
            return false;
        }
        return id.equals(that.id) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    @Override
    public String toString() {
        return type + ": " + id;
    }

    public enum NodeType {
        CLASS,
        METHOD
    }
}
