package examples;

public class Service {
    private final Repository repository;

    public Service() {
        this.repository = new Repository();
    }

    public String process(String id) {
        String value = repository.findById(id);
        repository.save(value);
        return value.toUpperCase();
    }
}
