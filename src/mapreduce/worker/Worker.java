package mapreduce.worker;

import mapreduce.Metrics;

import java.io.IOException;

public class Worker {

    public static void main(String[] args) throws IOException {
        WorkerParameters params = WorkerParameters.parse(args);

        Metrics.setup(params.name);
        Metrics.startReportingThread();

        new Thread(new WorkerServer(params)).start();
    }
}
