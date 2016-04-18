package mapreduce.worker;

import mapreduce.thrift.WorkerService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(WorkerServer.class);

    private final WorkerParameters params;

    public WorkerServer(WorkerParameters params) {
        this.params = params;
    }

    @Override
    public void run() {
        try {
            WorkerService.Processor<WorkerHandler> processor =
                    new WorkerService.Processor<>(new WorkerHandler(params));

            TServerTransport transport = new TServerSocket(50000);
            TThreadPoolServer.Args args = new TThreadPoolServer.Args(transport)
                    .maxWorkerThreads(3000)
                    .processor(processor);
            TServer server = new TThreadPoolServer(args);

            log.info("run, Starting worker server...");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
