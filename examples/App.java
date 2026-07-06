package examples;

public class App {
    public static void main(String[] args) {
        Repository repository = new Repository();
        Notifier notifier = new EmailNotifier();
        Service service = new Service(repository, notifier);
        String result = service.process("42");
        System.out.println(result);
    }
}
