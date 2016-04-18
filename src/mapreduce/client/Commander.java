package mapreduce.client;


import mapreduce.ThriftClient;
import mapreduce.thrift.MasterInfo;
import mapreduce.thrift.MasterService;

/**
 * Given a line of input, handles various operations.
 * Creates a coordinator client for localhost:50000.
 */
public class Commander {

    private final ThriftClient<MasterService.Client> masterClient =
            ThriftClient.makeCoordinatorClient("localhost");

    public void masterInfo() {
        MasterInfo info = masterClient.with(client -> client.info());

        System.out.println("Chunk size = " + info.chunkSize);
        System.out.println("Redundancy = " + info.redundancy);

        System.out.println("\nWorker servers -------");
        info.servers.forEach(System.out::println);
    }


}
