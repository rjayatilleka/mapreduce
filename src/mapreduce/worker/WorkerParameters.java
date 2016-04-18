package mapreduce.worker;

import java.util.UUID;

public class WorkerParameters {

    public final UUID workerId;
    public final int failPercent;

    public WorkerParameters(UUID workerId, int failPercent) {
        this.workerId = workerId;
        this.failPercent = failPercent;
    }

    public static WorkerParameters parse(String[] args) {
        UUID workerId = UUID.fromString(args[0]);
        int failPercent = Integer.parseInt(args[1]);

        return new WorkerParameters(workerId, failPercent);
    }
}
