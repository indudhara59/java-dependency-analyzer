package examples;

import java.util.List;

public class EmailNotifier implements Notifier {
    @Override
    public void notify(List<String> messages) {
        messages.forEach(System.out::println);
    }
}
