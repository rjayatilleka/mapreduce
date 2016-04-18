package mapreduce.client;


import mapreduce.ThriftClient;
import mapreduce.thrift.MasterInfo;
import mapreduce.thrift.MasterService;
import mapreduce.thrift.WorkerInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Given a line of input, handles various operations.
 * Creates a coordinator client for localhost:50000.
 */
public class Commander {

    private final ThriftClient<MasterService.Client> masterClient =
            ThriftClient.makeMasterClient("localhost");

    public void masterInfo() {
        MasterInfo info = masterClient.with(client -> client.info());

        System.out.println("Chunk size = " + info.chunkSize);
        System.out.println("Redundancy = " + info.redundancy);

        System.out.println("\nWorker servers -------");
        info.servers.forEach(System.out::println);
    }

    public void mergesort(String[] command) {
        String inputFilename = command[1];

        masterClient.run(client -> client.mergesort(inputFilename));

    }

    public void workerInfo(String[] command) {
        String host = command[1];
        int port = Integer.parseInt(command[2]);

        WorkerInfo info = ThriftClient.makeWorkerClient(0, host, port).with(client -> client.info());

        System.out.println("Name = " + info.workerId);
    }

    public void runSort(String[] command) {
        String host = command[1];
        int port = Integer.parseInt(command[2]);
        String inputId = command[3];

        String outputId = ThriftClient.makeWorkerClient(0, host, port)
                .with(client -> client.runSort(inputId));

        System.out.println("outputId = " + outputId);
    }

    public void runMerge(String[] command) {
        String host = command[1];
        int port = Integer.parseInt(command[2]);
        List<String> inputIds = new ArrayList<>();

        for (int i = 3; i < command.length; i++) {
            inputIds.add(command[i]);
        }

        String outputId = ThriftClient.makeWorkerClient(0, host, port)
                .with(client -> client.runMerge(inputIds));

        System.out.println("outputId = " + outputId);
    }
}
