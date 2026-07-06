package com.indudhara.dependency.model;

import java.util.Objects;

public final class DependencyEdge {
    private final String source;
    private final String target;
    private final EdgeType type;
    private final String reason;

    public DependencyEdge(String source, String target, EdgeType type, String reason) {
        this.source = Objects.requireNonNull(source, "source");
        this.target = Objects.requireNonNull(target, "target");
        this.type = Objects.requireNonNull(type, "type");
        this.reason = Objects.requireNonNull(reason, "reason");
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

    public String getReason() {
        return reason;
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
                && reason.equals(that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, type, reason);
    }

    @Override
    public String toString() {
        return source + " -> " + target + " [" + type.label + ": " + reason + "]";
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
}
