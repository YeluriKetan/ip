import tasks.Deadline;
import tasks.Event;
import tasks.Task;
import tasks.Todo;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class BotManager {
    private Scanner commandInput;
    private ArrayList<Task> tasks;
    private File taskDataFile;

    public BotManager() throws IOException {
        this.commandInput = new Scanner(System.in);
        this.tasks = new ArrayList<>();
        this.storageInit();
        this.listen();
    }

    private void storageInit() throws IOException {
        File taskDataDir = new File(Paths.get("data").toString());
        if (!taskDataDir.exists()) {
            taskDataDir.mkdir();
        }
        File taskDataFile = new File(Paths.get("data","taskData.txt").toString());
        if (taskDataFile.exists()) {
            Scanner loadScan = new Scanner(taskDataFile);
            while (loadScan.hasNextLine()) {
                String[] nextLineArr = loadScan.nextLine().split("%");
                switch (nextLineArr[0]) {
                case "T":
                    tasks.add(new Todo(nextLineArr[2], Boolean.parseBoolean(nextLineArr[1])));
                    break;
                case "E":
                    tasks.add(new Event(nextLineArr[2], nextLineArr[3], Boolean.parseBoolean(nextLineArr[1])));
                    break;
                case "D":
                    tasks.add(new Deadline(nextLineArr[2], nextLineArr[3], Boolean.parseBoolean(nextLineArr[1])));
                    break;
                default:
                    break;
                }
            }
            loadScan.close();
        } else {
            taskDataFile.createNewFile();
        }
        this.taskDataFile = taskDataFile;
    }
    private void listen() throws IOException {
        while (commandInput.hasNextLine()) {
            String command = commandInput.nextLine();
            try {
                if (command.equalsIgnoreCase("bye")) {
                    this.close();
                    break;
                } else if (command.equalsIgnoreCase("list")) {
                    this.list();
                } else if (Pattern.compile("done\\s+\\d+\\s*").matcher(command.toLowerCase()).matches()) {
                    this.markTaskDone(command.substring(4).trim());
                } else if (Pattern.compile("delete\\s+\\d+\\s*").matcher(command.toLowerCase()).matches()) {
                    this.deleteTask(command.substring(6).trim());
                } else if (Pattern.compile("(?i)todo.*").matcher(command).matches()) {
                    this.addTodo(command);
                } else if (Pattern.compile("(?i)deadline.*").matcher(command).matches()) {
                    this.addDeadline(command);
                } else if (Pattern.compile("(?i)event.*").matcher(command).matches()) {
                    this.addEvent(command);
                } else {
                    this.commandFail();
                }
            } catch (DukeException ex) {
                Response.respond(ex.getMessage());
            }
        }
        commandInput.close();
    }

    private void addTodo(String description) throws IOException {
        String[] strArr = Pattern.compile("(?i)todo\\s+").split(description, 2);
        if (strArr.length == 2 && strArr[1].length() > 0) {
            Todo newTodo = new Todo(strArr[1]);
            this.add(newTodo);
        } else if (strArr.length == 2 || strArr[0].length() == 4) {
            throw new DukeException("The description of a TODO task cannot be empty.\nPlease try again.");
        } else {
            throw new DukeException("There appears to be a typo in your TODO command.\n"
                    + "The command should be of the form:\n"
                    + "  todo 'description'\n"
                    + "Please try again.");
        }
    }

    private void addDeadline(String description) throws IOException {
        if (Pattern.compile("(?i)(deadline ).*\\S+.*( /by ).*\\S+.*").matcher(description).matches()) {
            String[] strArr = description.substring(9).split(" /by ", 2);
            Deadline newDeadline = new Deadline(strArr[0].trim(), strArr[1].trim());
            this.add(newDeadline);
        } else {
            throw new DukeException("There appears to be a typo in your DEADLINE command.\n" 
                    + "The command should be of the form:\n"
                    + "  deadline 'description' /by 'time'\n"
                    + "Please try again.");
        }
    }

    private void addEvent(String description) throws IOException {
        if (Pattern.compile("(?i)(event ).*\\S+.*( /at ).*\\S+.*").matcher(description).matches()) {
            String[] strArr = description.substring(6).split(" /at ", 2);
            Event newEvent = new Event(strArr[0].trim(), strArr[1].trim());
            this.add(newEvent);
        } else {
            throw new DukeException("There appears to be a typo in your EVENT command.\n"
                    + "The command should be of the form:\n"
                    + "  event 'description' /at 'time'\n"
                    + "Please try again.");
        }
    }

    private void add(Task newTask) throws IOException {
        tasks.add(newTask);
        FileWriter taskDataWriter = new FileWriter(taskDataFile, true);
        taskDataWriter.write(newTask.toStorage());
        taskDataWriter.close();
        Response.respond("Got it. I've added this task: \n"
        + "  " + tasks.get(tasks.size() - 1).toString() + "\n"
        + this.numOfTasks());
    }

    private void markTaskDone(String index) throws IOException {
        int indexInt = Integer.parseInt(index) - 1;
        if (indexInt < tasks.size() && indexInt > -1) {
            Task currTask = tasks.get(indexInt);
            currTask.markDone();
            BufferedReader taskDataReader = new BufferedReader(new FileReader(taskDataFile));
            StringBuilder newTaskData = new StringBuilder();
            for (int i = 0; i < indexInt; i++) {
                newTaskData.append(taskDataReader.readLine() + "\n");
            }
            taskDataReader.readLine();
            newTaskData.append(currTask.toStorage());
            while (true) {
                String nextLine = taskDataReader.readLine();
                if (nextLine == null) {
                    break;
                } else {
                    newTaskData.append(nextLine + "\n");
                }
            }
            FileWriter taskDataWriter = new FileWriter(taskDataFile);
            taskDataWriter.write(newTaskData.toString());
            taskDataWriter.close();
            taskDataReader.close();
            Response.respond("Nice! I've marked this task as done:\n"
                    + "  " + index + "." + currTask);
        } else {
            Response.respond("That task doesn't exist.\nPlease Try again.");
        }
    }

    private void deleteTask(String index) throws IOException {
        int indexInt = Integer.parseInt(index) - 1;
        if (indexInt < tasks.size() && indexInt > -1) {
            Task currTask = tasks.get(indexInt);
            tasks.remove(indexInt);
            BufferedReader taskDataReader = new BufferedReader(new FileReader(taskDataFile));
            StringBuilder newTaskData = new StringBuilder();
            for (int i = 0; i < indexInt; i++) {
                newTaskData.append(taskDataReader.readLine() + "\n");
            }
            taskDataReader.readLine();
            while (true) {
                String nextLine = taskDataReader.readLine();
                if (nextLine == null) {
                    break;
                } else {
                    newTaskData.append(nextLine + "\n");
                }
            }
            FileWriter taskDataWriter = new FileWriter(taskDataFile);
            taskDataWriter.write(newTaskData.toString());
            taskDataWriter.close();
            taskDataReader.close();
            Response.respond("Noted. I've removed this task:\n"
                    + "  " + index + "." + currTask.toString() + "\n"
                    + this.numOfTasks());
            
        } else {
            Response.respond("That task doesn't exist.\nPlease Try again.");
        }
    }
    
    private void list() {
        if (tasks.size() > 0) {
            Response.drawLine();
            System.out.println("Here are the tasks in your list:");
            for (int i = 0; i < tasks.size(); i++) {
                System.out.println("  " + (i + 1) + "." + tasks.get(i).toString());
            }
            Response.drawLine();
        } else {
            Response.respond(this.numOfTasks());
        }
    }

    private void commandFail() {
        throw new DukeException("I didn't get that. Please try again.");
    }
    
    private String numOfTasks() {
        if (tasks.size() > 0) {
            return "Now you have " + tasks.size() + " tasks in the list.";
        } else {
            return "There are no tasks in your list.";
        }
        
    }
    
    private void close() {
        Response.exitResponse();
    }
}
