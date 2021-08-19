import java.util.ArrayList;

public class Response {

    private static final String H_LINE = "------------------------------\n";

    public static void greetingResponse() {
        String logo = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";
        String helloMsg = H_LINE
                + "Hello! I'm Duke\n"
                + "What can I do for you?\n"
                + H_LINE;
        System.out.print("Hello from\n" + logo + helloMsg);
    }

    public static void respond(String message) {
        System.out.print(H_LINE + message + "\n" + H_LINE);
    }

    public static void drawLine() {
        System.out.print(H_LINE);
    }
    public static void exitResponse() {
        System.out.println(H_LINE + "Bye. Hope to see you again soon!\n" + H_LINE);
    }
}
