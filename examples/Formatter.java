package examples;

public final class Formatter {
    private Formatter() {
    }

    public static String normalize(String value) {
        return value.trim().toLowerCase();
    }
}
