import service.App;
import ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        App app = new App();
        app.seedDefaults();
        new ConsoleUI(app).run();
    }
}