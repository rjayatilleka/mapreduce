package mapreduce.worker;

import java.io.IOException;
import java.util.Scanner;

/**
 * Usage: <port> <fail percent>
 */
public class WorkerParameters {

    public final String name;
    public final int port;
    public final int failPercent;

    public WorkerParameters(String hostname, int port, int failPercent) {
        this.name = "worker-" + hostname + "-" + port;
        this.port = port;
        this.failPercent = failPercent;
    }

    public static WorkerParameters parse(String[] args) throws IOException {
        Process p = Runtime.getRuntime().exec("hostname");
        Scanner s = new Scanner(p.getInputStream());

        String hostname = s.nextLine();
        s.close();

        System.out.println("got hostname " + hostname);
        int port = Integer.parseInt(args[0]);
        int failPercent = Integer.parseInt(args[1]);

        return new WorkerParameters(hostname, port, failPercent);
    }
}
