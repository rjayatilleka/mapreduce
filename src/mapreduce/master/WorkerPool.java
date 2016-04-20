package mapreduce.master;

import mapreduce.ThriftClient;
import mapreduce.thrift.Address;
import mapreduce.thrift.WorkerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class WorkerPool {

    private static final Logger log = LoggerFactory.getLogger(WorkerPool.class);

    private final int count;
    private final List<Address> workers;
    private final ConcurrentHashMap<Address, Boolean> accessible = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public WorkerPool(List<Address> workers) {
        this.count = workers.size();
        this.workers = workers;

        for (Address w : workers) {
            this.accessible.put(w, Boolean.TRUE);
            startMonitor(w);
        }
    }

    private void startMonitor(final Address w) {
        ThriftClient<WorkerService.Client> c =
                ThriftClient.makeWorkerClient(100, w.hostname, w.port);

        Runnable heartbeat = () -> c.run(client -> client.heartbeat());

        Observable.interval(1000, TimeUnit.MILLISECONDS)
                .map(i -> heartbeat)
                .map(executor::submit)
                .flatMap(future ->
                    Observable.from(future)
                            .map(o -> true)
                            .onErrorReturn(e -> false))
                .distinctUntilChanged()
                .subscribe(status -> this.setWorker(w, status));
    }

    public ThriftClient<WorkerService.Client> getWorker(int timeoutMs) {
        while (true) {
            Address w = workers.get(random.nextInt(count));

            if (accessible.get(w)) {
                //log.info("getWorker, found worker, w = {}", w);
                return ThriftClient.makeWorkerClient(timeoutMs, w.hostname, w.port);
            } else {
                //log.info("getWorker, bad worker, w = {}", w);
            }
        }
    }

    public void setWorker(Address w, boolean status) {
        log.info("setWorker, w = {}, status = {}", w, status);
        accessible.put(w, status);
    }
}
