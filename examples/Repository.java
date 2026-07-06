package examples;

import java.util.Optional;

public class Repository {
    public Optional<String> findById(String id) {
        return Optional.of("record-" + id);
    }

    public void save(String value) {
        System.out.println("Saving " + value);
    }
}
