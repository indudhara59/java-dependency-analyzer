package examples;

public class App {
    public static void main(String[] args) {
        Service service = new Service();
        String result = service.process("42");
        System.out.println(result);
    }
}
