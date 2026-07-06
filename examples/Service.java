package examples;

import java.util.List;

public class Service {
    private final Repository repository;
    private final Notifier notifier;

    public Service(Repository repository, Notifier notifier) {
        this.repository = repository;
        this.notifier = notifier;
    }

    public String process(String id) {
        String value = repository.findById(id).orElse("missing");
        String formatted = Formatter.normalize(value);
        repository.save(formatted);
        notifier.notify(List.of(formatted));
        return formatted.toUpperCase();
    }
}
