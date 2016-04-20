package mapreduce.master;

import mapreduce.Metrics;

import java.io.IOException;

public class Master {

    public static void main(String[] args) throws IOException {
        MasterParameters params = MasterParameters.parse(args);

        Metrics.setup(params.name);

        WorkerPool pool = new WorkerPool(params.servers);
        new Thread(new MasterServer(params, pool)).run();
    }
}
