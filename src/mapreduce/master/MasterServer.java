package mapreduce.master;

import mapreduce.thrift.MasterService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MasterServer.class);

    private final MasterParameters params;
    private final WorkerPool pool;

    public MasterServer(MasterParameters params, WorkerPool pool) {
        this.params = params;
        this.pool = pool;
    }

    @Override
    public void run() {
        try {
            MasterService.Processor<MasterHandler> processor =
                    new MasterService.Processor<>(new MasterHandler(params, pool));

            TServerTransport transport = new TServerSocket(50000);
            TThreadPoolServer.Args args = new TThreadPoolServer.Args(transport)
                    .maxWorkerThreads(3000)
                    .processor(processor);
            TServer server = new TThreadPoolServer(args);

            log.info("run, Starting master server...");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
