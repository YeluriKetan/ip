package duke;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class Parser {

    /**
     * Returns a String array that contains the useful and necessary parts of a command to be executed.
     *
     * @param newCommand String version of a command.
     * @return String array.
     */
    public static String[] parse(String newCommand) {
        if (Pattern.compile("(?i)bye\\s*").matcher(newCommand).matches()) {
            return new String[]{"bye"};
        } else if (Pattern.compile("(?i)list\\s*").matcher(newCommand).matches()) {
            return new String[]{"list"};
        } else if (Pattern.compile("(?i)done\\s+\\d+\\s*").matcher(newCommand).matches()) {
            return new String[]{"done", newCommand.substring(4).trim()};
        } else if (Pattern.compile("(?i)delete\\s+\\d+\\s*").matcher(newCommand).matches()) {
            return new String[]{"delete", newCommand.substring(6).trim()};
        } else if (Pattern.compile("(?i)todo.*").matcher(newCommand).matches()) {
            return parseTodo(newCommand);
        } else if (Pattern.compile("(?i)deadline.*").matcher(newCommand).matches()) {
            return parseDeadline(newCommand);
        } else if (Pattern.compile("(?i)event.*").matcher(newCommand).matches()) {
            return parseEvent(newCommand);
        } else if (Pattern.compile("(?i)find.*").matcher(newCommand).matches()) {
            return parseFind(newCommand);
        } else {
            return new String[]{"fail"};
        }
    }

    /**
     * Returns a LocalDateTime object after parsing a string version of date and time.
     *
     * @param dateTime String version of date and time.
     * @return LocalDateTime object.
     */
    public static LocalDateTime parseDateTime(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd HHmm");
        LocalDateTime deadlineDT;

        try {
            deadlineDT = LocalDateTime.parse(dateTime, formatter);
            return deadlineDT;
        } catch (DateTimeException ex) {
            throw new DukeException(ex.getMessage() + "\n"
                    + "Please try again.");
        }
    }

    /**
     * Returns the integer version of a string number.
     *
     * @param intString String version of a number.
     * @return integer version of the given number.
     */
    public static int parseIndex(String intString) {
        return Integer.parseInt(intString.trim());
    }

    private static String[] parseTodo(String newCommand) {
        String[] strArr = Pattern.compile("(?i)todo\\s+").split(newCommand, 2);

        if (strArr.length == 2 && strArr[1].length() > 0) {
            return new String[]{"add", "todo", strArr[1]};
        } else if (strArr.length == 2 || strArr[0].length() == 4) {
            throw new DukeException("The description of a TODO task cannot be empty.\nPlease try again.");
        } else {
            throw new DukeException("There appears to be a typo in your TODO command.\n"
                    + "The command should be of the form:\n"
                    + "  todo 'description'\n"
                    + "Please try again.");
        }
    }

    private static String[] parseDeadline(String newCommand) {
        if (Pattern.compile("(?i)(deadline ).*\\S+.*( /by )\\d{4}\\s\\d{2}\\s\\d{2}\\s\\d{4}")
                .matcher(newCommand).matches()) {
            String[] strArr = newCommand.substring(8).split(" /by ", 2);
            return new String[]{"add", "deadline", strArr[0].trim(), strArr[1].trim()};
        } else {
            throw new DukeException("There appears to be a typo in your DEADLINE command.\n"
                    + "The command should be of the form:\n"
                    + "  deadline 'description' /by 'yyyy mm dd hhmm'\n"
                    + "Please try again.");
        }
    }

    private static String[] parseEvent(String newCommand) {
        if (Pattern.compile("(?i)(event ).*\\S+.*( /from )\\d{4}\\s\\d{2}\\s\\d{2}\\s\\d{4}"
                + "( to )\\d{4}\\s\\d{2}\\s\\d{2}\\s\\d{4}").matcher(newCommand).matches()) {
            String[] strArr = newCommand.substring(5).split(" /from ", 2);
            String[] dateTimeArr = strArr[1].split(" to ", 2);
            return new String[]{"add", "event", strArr[0].trim(), dateTimeArr[0], dateTimeArr[1]};
        } else {
            throw new DukeException("There appears to be a typo in your EVENT command.\n"
                    + "The command should be of the form:\n"
                    + "  event 'description' /from 'yyyy mm dd hhmm' to 'yyyy mm dd hhmm'\n"
                    + "Please try again.");
        }
    }

    private static String[] parseFind(String newCommand) {
        String[] strArr = Pattern.compile("(?i)find\\s+").split(newCommand, 2);
        if (strArr.length == 2 && strArr[1].length() > 0) {
            return new String[]{"find", strArr[1].trim()};
        } else if (strArr.length == 2 || strArr[0].length() == 4) {
            throw new DukeException("The contents of a FIND command cannot be empty.\nPlease try again.");
        } else {
            throw new DukeException("There appears to be a typo in your FIND command.\n"
                    + "The command should be of the form:\n"
                    + "  find 'content'\n"
                    + "Please try again.");
        }
    }
}
