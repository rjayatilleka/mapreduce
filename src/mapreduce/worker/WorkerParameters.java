package mapreduce.worker;

/**
 * Usage: <hostname> <port> <fail percent>
 */
public class WorkerParameters {

    public final String name;
    public final int port;
    public final int failPercent;

    public WorkerParameters(String hostname, int port, int failPercent) {
        this.name = hostname + "-" + port;
        this.port = port;
        this.failPercent = failPercent;
    }

    public static WorkerParameters parse(String[] args) {
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        int failPercent = Integer.parseInt(args[2]);

        return new WorkerParameters(hostname, port, failPercent);
    }
}
