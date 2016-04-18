package mapreduce.client;

import java.util.Scanner;

/**
 * Application that takes user input line-by-line, parses it,
 * and runs commands with it.
 *
 * MUST with on same server as coordinator.
 */
public class Client {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        Commander commander = new Commander();

        while (s.hasNextLine()) {
            String line = s.nextLine().trim();
            if (line.isEmpty()) continue;

            System.out.println("------ BEGIN " + line + " ------");
            process(commander, line);
            System.out.println("------ END ------\n\n");
        }
    }

    private static void process(Commander commander, String line) {
        String[] command = line.split("\\s+");

        try {
            switch (command[0]) {
                case "masterInfo":
                    commander.masterInfo();
                    break;
                case "mergesort":
                    commander.mergesort(command);
                    break;
                default:
                    throw new RuntimeException("Unknown command");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}