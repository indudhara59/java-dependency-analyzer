package examples;

public class Repository {
    public String findById(String id) {
        return "record-" + id;
    }

    public void save(String value) {
        System.out.println("Saving " + value);
    }
}
